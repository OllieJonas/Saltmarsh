package me.olliejonas.saltmarsh.scheduledevents;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Getter
@Setter
public final class ScheduledEventManagerImpl implements ScheduledEventManager {

    static BiFunction<String, String, String> EVENT_CREATED_MESSAGE = (mention, creator) ->
            mention + " " + creator + " has created an event! Check it out in the Events tab! :heart:";

    static BiFunction<String, String, String> EVENT_CANCELLED_MESSAGE =
            (mention, name) -> mention + " " + name + " has unfortunately been cancelled! :cry:";


    private final RecurringEventManager recurringEventManager;

    private final Connection connection;

    private Map<String, String> guildToPingChannelMap;

    // JDA loses track of who created the event whenever the bot reloads an event from the database
    // (for some reason I don't know why)
    private Map<String, String> eventToCreatorMap;

    // tuple2 for event notif message and accompanying ping message
    // v1 = embed (event), v2 = ping message. v1 can't be null, v2 can be
    private Map<String, Map<String, Tuple2<String, String>>> eventToChannelToMessageMap;


    private Map<String, String> guildToRolesMap;

    public ScheduledEventManagerImpl(RecurringEventManager recurringEventManager) {
        this(recurringEventManager, null, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public ScheduledEventManagerImpl(RecurringEventManager recurringEventManager,
                                     Connection connection, Map<String, String> guildToPingChannelMap,
                                     Map<String, Map<String, Tuple2<String, String>>> eventToChannelToMessageMap,
                                     Map<String, String> guildToRolesMap, Map<String, String> eventToCreatorMap) {

        this.recurringEventManager = recurringEventManager;
        this.connection = connection;
        this.guildToPingChannelMap = guildToPingChannelMap;
        this.eventToChannelToMessageMap = eventToChannelToMessageMap;
        this.guildToRolesMap = guildToRolesMap;
        this.eventToCreatorMap = eventToCreatorMap;
    }

    public Optional<Role> getRole(Guild guild) {
        if (!guildToRolesMap.containsKey(guild.getId())) return Optional.empty();

        return Optional.ofNullable(guild.getRoleById(guildToRolesMap.get(guild.getId())));
    }

    public boolean toggleRole(@NotNull Guild guild, @NotNull Role role) {
        String guildId = guild.getId();
        String roleId = role.getId();

        if (guildToRolesMap.containsKey(guildId) && guildToRolesMap.get(guildId).equals(roleId)) {
            guildToRolesMap.remove(guildId);
            return true;
        }

        guildToRolesMap.put(guildId, roleId);
        registerRoleInSql(guildId, roleId);
        return false;
    }

    public void addCreator(ScheduledEvent event) {
        addCreator(event.getId(), event.getCreatorId());
    }

    public Optional<CacheRestAction<Member>> getCreatorAction(Guild guild, ScheduledEvent event) {
        if (!eventToCreatorMap.containsKey(event.getId())) return Optional.empty();

        return Optional.of(guild.retrieveMemberById(eventToCreatorMap.get(event.getId())));
    }


    // returns whether the channel was already there (so true = removed, false = added)
    public boolean toggleChannel(@NotNull Guild guild, @NotNull TextChannel channel) {
        String guildId = guild.getId();
        String channelId = channel.getId();

        if (guildToPingChannelMap.containsKey(guildId) && guildToPingChannelMap.get(guildId).equals(channelId)) {
            guildToPingChannelMap.remove(guildId);
            return true;
        }

        guildToPingChannelMap.put(guildId, channelId);
        registerChannelInSql(guildId, channelId);
        return false;
    }

    public Optional<TextChannel> getChannel(Guild guild) {
        if (!guildToPingChannelMap.containsKey(guild.getId())) return Optional.empty();

        return Optional.ofNullable(guild.getTextChannelById(guildToPingChannelMap.get(guild.getId())));
    }

    public void send(TextChannel channel, ScheduledEventNotification notification) {
        Optional<Role> pingRole = getRole(channel.getGuild());

        BiConsumer<? super TextChannel, ? super Message> sendMessage = (ch, pingMessage) ->
                ch.sendMessageEmbeds(notification.toEmbed())
                        .queue(success -> registerMessage(notification, channel, success, pingMessage));

        pingRole.ifPresentOrElse(role -> channel.sendMessage(
                EVENT_CREATED_MESSAGE.apply(role.getAsMention(), notification.creator().getEffectiveName()))
                        .queue(ping -> sendMessage.accept(channel, ping)),
                () -> sendMessage.accept(channel, null));
    }

    public void edit(TextChannel channel, ScheduledEventNotification notification) {
        if (notification.status() == ScheduledEvent.Status.COMPLETED || notification.status() == ScheduledEvent.Status.CANCELED)
            removeScheduledEventFromSql(notification.eventId());

        channel.retrieveMessageById(eventToChannelToMessageMap.get(notification.eventId()).get(channel.getId()).v1())
                .queue(message -> message.editMessage(MessageEditData.fromEmbeds(notification.toEmbed())).queue());
    }

    public void delete(Guild guild, TextChannel channel, ScheduledEventNotification notification) {
        getRole(guild).ifPresent(role -> channel.sendMessage(
                EVENT_CANCELLED_MESSAGE.apply(role.getAsMention(), notification.name())).queue());

        edit(channel, notification);
    }

    private void removePingMessages(ScheduledEvent event) {
        eventToChannelToMessageMap.get(event.getId()).entrySet().stream()
                .filter(e -> e.getValue().v2() != null && !e.getValue().v2().isEmpty())
                .forEach(e -> Objects.requireNonNull(event.getGuild().getTextChannelById(e.getKey()))
                        .deleteMessageById(e.getValue().v2()).queue());
    }

    public boolean isEventUnregistered(ScheduledEvent scheduledEvent) {
        return !eventToChannelToMessageMap.containsKey(scheduledEvent.getId());
    }

    public void destroy(ScheduledEvent scheduledEvent) {
        removePingMessages(scheduledEvent);
        eventToChannelToMessageMap.remove(scheduledEvent.getId());
        removeScheduledEventFromSql(scheduledEvent.getId());
    }

    private void addCreator(String eventId, String creatorId) {
        if (eventToCreatorMap.containsKey(eventId)) return;

        eventToCreatorMap.put(eventId, creatorId);
    }

    private void registerMessage(ScheduledEventNotification event, TextChannel channel, @NotNull Message embedMessage, @Nullable Message pingMessage) {
        registerMessage(event.id(), channel.getId(),
                event.creator().getId(), embedMessage.getId(), pingMessage != null ? pingMessage.getId() : "");
    }

    private void registerMessage(String eventId, String channelId, String creatorId, String messageId, String pingMessageId) {
        if (!eventToChannelToMessageMap.containsKey(eventId))
            eventToChannelToMessageMap.put(eventId, new HashMap<>());

        eventToChannelToMessageMap.get(eventId).put(channelId, new Tuple2<>(messageId, pingMessageId));
        registerEventInSql(eventId, channelId, creatorId, messageId, pingMessageId);
    }

    private void removeScheduledEventFromSql(String event) {
        if (connection == null) return;

        String stmtStr = "DELETE FROM " + Constants.DB.SCHEDULED_EVENTS + " WHERE event = ?;";

        try (PreparedStatement statement = connection.prepareStatement(stmtStr)) {
            statement.setString(1, event);

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerEventInSql(String event, String channel, String creator, String embedMessage, String pingMessage) {
        if (connection == null) return;

        String stmtStr = "INSERT INTO " + Constants.DB.SCHEDULED_EVENTS +
                "(`event`, `channel`, `creator`, `embed_message`, `ping_message`) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(stmtStr)) {
            statement.setString(1, event);
            statement.setString(2, channel);
            statement.setString(3, creator);
            statement.setString(4, embedMessage);
            statement.setString(5, pingMessage);

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerRoleInSql(String guild, String role) {
        registerMeta(guild, "role", role);
    }

    private void registerChannelInSql(String guild, String channel) {
        registerMeta(guild, "channel", channel);
    }

    // dodgy string construction here should be alright because users can't input into column.
    private void registerMeta(String guild, String column, String value) {
        String stmtStr = "INSERT INTO " + Constants.DB.SCHEDULED_EVENTS_META +
                "(`guild`, `" + column + "`) " +
                "VALUES (?, ?) AS new_entry ON DUPLICATE KEY UPDATE " + column + " = new_entry." + column + ";";

        try (PreparedStatement statement = connection.prepareStatement(stmtStr)) {

            statement.setString(1, guild);
            statement.setString(2, value);

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
