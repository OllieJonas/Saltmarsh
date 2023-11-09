package me.olliejonas.saltmarsh.scheduledevents.recurring;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.SQLManager;
import me.olliejonas.saltmarsh.embed.wizard.CommonWizardMenus;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepText;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ScheduledEventAction;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public final class RecurringEventManagerImpl extends SQLManager implements RecurringEventManager {

    private final WizardEmbedManager wizardEmbedManager;

    private ScheduledEventListener scheduledEventListener;

    private Map<String, String> guildToChannelMap;

    private Map<String, RecurringEvent> eventIdToRecurringEvents;

    public RecurringEventManagerImpl(WizardEmbedManager wizardEmbedManager) {
        this(null, wizardEmbedManager, new HashMap<>(), new HashMap<>());
    }

    public RecurringEventManagerImpl(Connection connection, WizardEmbedManager wizardEmbedManager,
                                     Map<String, String> guildToChannelMap,
                                     Map<String, RecurringEvent> eventIdToRecurringEvents) {

        super(connection);

        this.wizardEmbedManager = wizardEmbedManager;


        this.guildToChannelMap = guildToChannelMap;
        this.eventIdToRecurringEvents = eventIdToRecurringEvents;
    }

    public void addChannel(Guild guild, TextChannel channel) {
        insertChannelIntoSql(guild.getId(), channel.getId());
        guildToChannelMap.put(guild.getId(), channel.getId());
    }

    public void register(RecurringEvent event, Guild guild) {
        ScheduledEvent schEvent = event.scheduledEvent();

        eventIdToRecurringEvents.put(schEvent.getId(), event);
        insertRecurringEventIntoSql(event);
        scheduledEventListener.onScheduledEventUpdateRecurring(guild, schEvent);
    }

    public void remove(String eventId) {
        eventIdToRecurringEvents.remove(eventId);
        removeRecurringEventFromSql(eventId);
    }

    public Optional<RecurringEvent> get(String eventId) {
        return Optional.ofNullable(eventIdToRecurringEvents.get(eventId));
    }

    public boolean isRecurring(ScheduledEvent event) {
        return get(event.getId()).isPresent();
    }

    public Optional<TextChannel> getChannel(Guild guild) {
        if (!guildToChannelMap.containsKey(guild.getId())) return Optional.empty();

        return Optional.ofNullable(guild.getTextChannelById(guildToChannelMap.get(guild.getId())));
    }

    InteractionResponses sendUpdateEventEmbed(Guild guild, ScheduledEvent event, ScheduledEvent.Status reason) {
        TextChannel channel = getChannel(guild).orElseThrow();  // should never throw - this is already checked

        return wizardEmbedManager.register(null,
                channel, buildUpdateEventEmbed(guild, event, reason));
    }


    private WizardEmbed buildUpdateEventEmbed(Guild guild, ScheduledEvent event,
                                              ScheduledEvent.Status reason) {
        String title = "Recurring Events Wizard";

        WizardEmbed.Builder builder = WizardEmbed.builder().disableExitButton();

        boolean cancelled = reason == ScheduledEvent.Status.CANCELED;

        if (cancelled)
            builder.step(CommonWizardMenus.YES_NO("cancellation", title, "The event "
                    + event.getName() + " was cancelled!" +
                    " Would you like to remove this as a recurring event entirely?", 4, 1));

        return builder
                .step(CommonWizardMenus.YES_NO("yesno",
                        title,
                        (cancelled ? "" : "The event has has been marked as recurring has just finished! ") +
                                "Would you like to edit anything about it before sending out the notification" +
                                " on its completion?", 1, 3
                ))

                .step(StepText.of("title", title, "Would you like to change the title? " +
                                        "(You can type in 'NO' if you don'!)", String.class
                        ))

                .step(StepText.of("description", title, "Would you like to edit the description? " +
                        "(You can type in 'NO' if you don'!)", String.class))

                .onCompletion(ctx -> {
                    if (ctx.containsKey("cancellation") && (Boolean) ctx.get("cancellation")) {
                        remove(event.getId());
                        return InteractionResponses.messageAsEmbed("Successfully removed event as recurring!");
                    }

                    Optional<RecurringEvent> recurringEventOpt = get(event.getId());

                    if (recurringEventOpt.isEmpty()) return InteractionResponses.error("Error scheduling next recurring event" +
                            " (recurring event for this id isn't in hashmap! (talk to ollie " +
                            "they'll understand this))");

                    RecurringEvent recurringEvent = recurringEventOpt.get();

                    String t = (String) ctx.get("title");
                    String desc = (String) ctx.get("description");

                    scheduleNextEvent(guild, recurringEvent, t, desc);
                    return InteractionResponses.messageAsEmbed("Successfully scheduled next event!", true);
                })
                .build();
    }

    public void scheduleNextEvent(Guild guild, RecurringEvent recurringEvent,
                                  @Nullable String newTitle, @Nullable String newDesc) {

        newTitle = newTitle == null || newTitle.equalsIgnoreCase("NO") ?
                recurringEvent.scheduledEvent().getName() : newTitle;

        newDesc = newDesc == null || newDesc.equalsIgnoreCase("NO") ?
                recurringEvent.scheduledEvent().getDescription() : newDesc;

        ScheduledEvent scheduledEvent = recurringEvent.scheduledEvent();
        OffsetDateTime newStart = recurringEvent.frequency().next(scheduledEvent.getStartTime());
        OffsetDateTime newEnd = recurringEvent.frequency().next(scheduledEvent.getEndTime());

        ScheduledEventAction action = newEnd != null ?
                guild.createScheduledEvent(newTitle, scheduledEvent.getLocation(), newStart, newEnd) :
                guild.createScheduledEvent(newTitle, scheduledEvent.getChannel(), newStart);

        action = action.setDescription(newDesc);

        if (scheduledEvent.getImageUrl() != null) {
            try {
                action = action.setImage(Icon.from(new URL(scheduledEvent.getImageUrl()).openStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        action.queue(event -> registerNext(scheduledEvent.getId(), event.getId(),
                RecurringEvent.of(event, recurringEvent.creator(), recurringEvent.frequency())));
    }

    private void registerNext(String oldEventId, String newEventId, RecurringEvent nextEvent) {
        updateNewEventInSql(oldEventId, newEventId);
        eventIdToRecurringEvents.remove(oldEventId);
        eventIdToRecurringEvents.put(newEventId, nextEvent);
    }

    private void insertChannelIntoSql(String guild, String textChannel) {
        String stmtStr = "INSERT INTO " + Constants.DB.RECURRING_EVENTS_META +
                "(`guild`, `text_channel`) VALUES (?, ?) AS new_channel " +
                "ON DUPLICATE KEY UPDATE text_channel=new_channel.text_channel;";

        prepareStatement(stmtStr).ifPresent(statement -> {
            try {
                statement.setString(1, guild);
                statement.setString(2, textChannel);

                statement.execute();
                statement.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void insertRecurringEventIntoSql(RecurringEvent recurring) {
        insertRecurringEventIntoSql(recurring.scheduledEvent().getId(),
                recurring.scheduledEvent().getGuild().getId(), recurring.creator().getId(),
                recurring.frequency().name());
    }

    private void insertRecurringEventIntoSql(String event, String guild, String creator, String frequency) {
        String stmtStr = "INSERT INTO " + Constants.DB.RECURRING_EVENTS +
                "(`event`, `guild`, `creator`, `frequency`) VALUES (?, ?, ?, ?) AS new_event " +
                "ON DUPLICATE KEY UPDATE frequency=new_event.frequency;";

        prepareStatement(stmtStr).ifPresent(statement -> {
            try {
                statement.setString(1, event);
                statement.setString(2, guild);
                statement.setString(3, creator);
                statement.setString(4, frequency);

                statement.execute();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void updateNewEventInSql(String oldEvent, String newEvent) {
        String stmtStr = "UPDATE " + Constants.DB.RECURRING_EVENTS + " SET event = ? WHERE event = ?";

        prepareStatement(stmtStr).ifPresent(statement -> {
            try {
                statement.setString(1, newEvent);
                statement.setString(2, oldEvent);

                statement.execute();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void removeRecurringEventFromSql(String event) {
        String stmtStr = "DELETE FROM " + Constants.DB.RECURRING_EVENTS + " WHERE event = ?;";

        prepareStatement(stmtStr).ifPresent(statement -> {
            try {
                statement.setString(1, event);
                statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
