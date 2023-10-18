package me.olliejonas.saltmarsh.scheduledevents;

import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScheduledEventManager {

    private final Map<String, Set<String>> guildToPingChannelMap;

    // tuple2 for event notif message and accompanying ping message
    // v1 = embed (event), v2 = ping. v1 can't be null, v2 can be
    private final Map<String, Map<String, Tuple2<String, String>>> eventToChannelToMessageMap;

    private final Map<String, String> guildToRolesMap;

    public ScheduledEventManager() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public ScheduledEventManager(Map<String, Set<String>> guildToPingChannelMap) {
        this(guildToPingChannelMap, new HashMap<>(), new HashMap<>());
    }

    public ScheduledEventManager(Map<String, Set<String>> guildToPingChannelMap,
                                 Map<String, Map<String, Tuple2<String, String>>> eventToChannelToMessageMap,
                                 Map<String, String> guildToRoleMap) {
        this.guildToPingChannelMap = guildToPingChannelMap;
        this.eventToChannelToMessageMap = eventToChannelToMessageMap;
        this.guildToRolesMap = guildToRoleMap;
    }

    // true if removed, false if added
    public boolean addRole(Guild guild, Role role) {
        String guildId = guild.getId();
        String roleId = role.getId();

        if (guildToRolesMap.containsKey(guildId) && guildToRolesMap.get(guildId).equals(roleId)) {
            guildToRolesMap.remove(guildId);
            return true;
        }

        guildToRolesMap.put(guildId, roleId);
        return false;
    }

    public Role getRole(Guild guild) {
        if (!guildToRolesMap.containsKey(guild.getId())) return null;

        return guild.getRoleById(guildToRolesMap.get(guild.getId()));
    }

    public Set<TextChannel> getChannelsFor(Guild guild) {
        if (!guildToPingChannelMap.containsKey(guild.getId())) return Collections.emptySet();

        return guildToPingChannelMap.get(guild.getId()).stream().map(guild::getTextChannelById).collect(Collectors.toSet());
    }

    // returns whether the channel was already there (so true = removed, false = added)
    public boolean addNotificationChannel(@NotNull Guild guild, @NotNull TextChannel channel) {
        String guildId = guild.getId();
        String channelId = channel.getId();

        boolean guildRegistered = guildToPingChannelMap.containsKey(guildId);

        if (!guildRegistered)
            guildToPingChannelMap.put(guildId, new HashSet<>());

        boolean contains = guildToPingChannelMap.get(guildId).contains(channelId);

        if (contains)
            this.guildToPingChannelMap.get(guildId).remove(channelId);
        else
            this.guildToPingChannelMap.get(guildId).add(channelId);

        return contains;
    }

    public void registerAndSend(TextChannel channel, ScheduledEvent scheduledEvent, RecurringEventManager manager) {
        Role pingRole = getRole(channel.getGuild());
        ScheduledEventNotification notification = ScheduledEventNotification.fromEvent(scheduledEvent, manager);

        BiConsumer<? super TextChannel, ? super Message> sendMessage = (ch, pingMessage) ->
                ch.sendMessageEmbeds(notification.toEmbed())
                        .queue(success -> registerMessage(scheduledEvent, channel, success, pingMessage));

        if (pingRole != null) {
            channel.sendMessage(pingRole.getAsMention() + " " +
                            notification.creator().getEffectiveName() + " has made an event!")
                    .queue(ping -> sendMessage.accept(channel, ping));
        } else sendMessage.accept(channel, null);
    }

    public void registerMessage(ScheduledEvent event, TextChannel channel, @NotNull Message embedMessage, @Nullable Message pingMessage) {
        registerMessage(event.getId(), channel.getId(), embedMessage.getId(), pingMessage != null ? pingMessage.getId() : "");
    }
    public void registerMessage(String eventId, String channelId, String messageId, String pingMessageId) {
        if (!eventToChannelToMessageMap.containsKey(eventId))
            eventToChannelToMessageMap.put(eventId, new HashMap<>());

        eventToChannelToMessageMap.get(eventId).put(channelId, new Tuple2<>(messageId, pingMessageId));
    }

    public void callbackMessage(ScheduledEvent event, TextChannel channel, Consumer<? super Message> onSuccess) {
        channel.retrieveMessageById(eventToChannelToMessageMap.get(event.getId()).get(channel.getId()).v1()).queue(onSuccess);
    }

    public void removePingMessages(ScheduledEvent event) {
        eventToChannelToMessageMap.get(event.getId()).entrySet().stream()
                .filter(e -> e.getValue().v2() != null && !e.getValue().v2().equals(""))
                .forEach(e -> Objects.requireNonNull(event.getGuild().getTextChannelById(e.getKey()))
                        .deleteMessageById(e.getValue().v2()).queue());
    }

    public boolean isEventUnregistered(ScheduledEvent scheduledEvent) {
        return !eventToChannelToMessageMap.containsKey(scheduledEvent.getId());
    }

    public void destroyEvent(ScheduledEvent scheduledEvent) {
        eventToChannelToMessageMap.remove(scheduledEvent.getId());
    }
}
