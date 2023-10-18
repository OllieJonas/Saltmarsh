package me.olliejonas.saltmarsh.scheduledevents.recurring;

import lombok.Getter;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.input.CommonInputMenus;
import me.olliejonas.saltmarsh.embed.input.InputEmbed;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.embed.input.types.InputText;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ScheduledEventAction;
import org.jooq.lambda.tuple.Tuple2;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class RecurringEventManager {

    private final Map<String, String> guildToChannelRecurringEventMap;

    private final Map<String, RecurringEvent> eventIdToRecurringEvents;

    public RecurringEventManager() {
        this.eventIdToRecurringEvents = new HashMap<>();
        this.guildToChannelRecurringEventMap = new HashMap<>();
    }

    public void addChannel(Guild guild, TextChannel channel) {
        guildToChannelRecurringEventMap.put(guild.getId(), channel.getId());
    }

    public TextChannel getTextChannelForRecurringDump(Guild guild) {
        if (!guildToChannelRecurringEventMap.containsKey(guild.getId())) return null;

        return guild.getTextChannelById(guildToChannelRecurringEventMap.get(guild.getId()));
    }

    public void registerEventAsRecurring(ScheduledEvent schEvent, RecurringEvent event,
                                         Guild guild, ScheduledEventListener scheduledEventListener) {
        eventIdToRecurringEvents.put(schEvent.getId(), event);
        scheduledEventListener.onScheduledEventUpdateRecurring(guild, schEvent);
    }

    public void removeEvent(String eventId) {
        eventIdToRecurringEvents.remove(eventId);
    }

    public Optional<RecurringEvent> getEvent(String eventId) {
        System.out.println(eventId);
        System.out.println(String.join(", ", eventIdToRecurringEvents.keySet()));
        return Optional.ofNullable(eventIdToRecurringEvents.get(eventId));
    }


    public boolean isRecurring(ScheduledEvent event) {
        return getEvent(event.getId()).isPresent();
    }

    public void addNextToMap(String oldEventId, String newEventId, RecurringEvent nextEvent) {
        eventIdToRecurringEvents.remove(oldEventId);
        eventIdToRecurringEvents.put(newEventId, nextEvent);
    }

    public InteractionResponses sendUpdateEventEmbed(InputEmbedManager inputEmbedManager,
                                                     ScheduledEventListener scheduledEventListener,
                                        Guild guild, ScheduledEvent event, ScheduledEvent.Status reason) {
        TextChannel channel = getTextChannelForRecurringDump(guild);

        Tuple2<InteractionResponses, Boolean> embed = inputEmbedManager.createEmbed(null,
                channel, buildUpdateEventEmbed(guild, event, reason, scheduledEventListener));

        if (!embed.v2()) {
            return InteractionResponses.error("this needs to do stuff (Error 001 for explanation)");
        }
        return embed.v1();
    }


    public InputEmbed buildUpdateEventEmbed(Guild guild, ScheduledEvent event,
                                            ScheduledEvent.Status reason,
                                            ScheduledEventListener scheduledEventListener) {
        String title = "Recurring Events Wizard";

        InputEmbed.Builder builder = InputEmbed.builder().disableExitButton();

        boolean cancelled = reason == ScheduledEvent.Status.CANCELED;

        if (cancelled)
            builder.step(CommonInputMenus.YES_NO("cancellation", title, "The event "
                    + event.getName() + " was cancelled!" +
                    " Would you like to remove this as a recurring event entirely?", 4, 1));

        return builder
                .step(CommonInputMenus.YES_NO("yesno",
                        title,
                        (cancelled ? "" : "The event has has been marked as recurring has just finished! ") +
                                "Would you like to edit anything about it before sending out the notification" +
                                " on its completion?", 1, 3
                ))

                .step(InputText.of("title", title, "Would you like to change the title? " +
                                        "(You can type in 'NO' if you don'!)", String.class
                        ))

                .step(InputText.of("description", title, "Would you like to edit the description? " +
                        "(You can type in 'NO' if you don'!)", String.class))

                .onCompletion(ctx -> {
                    if (ctx.containsKey("cancellation") && (Boolean) ctx.get("cancellation")) {
                        removeEvent(event.getId());
                        return InteractionResponses.messageAsEmbed("Successfully removed event as recurring!");
                    }

                    Optional<RecurringEvent> recurringEventOpt = getEvent(event.getId());

                    if (recurringEventOpt.isEmpty()) return InteractionResponses.error("Error scheduling next recurring event" +
                            " (recurring event for this id isn't in hashmap! (talk to ollie " +
                            "they'll understand this))");

                    RecurringEvent recurringEvent = recurringEventOpt.get();

                    String t = (String) ctx.get("title");
                    t = t == null || t.equalsIgnoreCase("NO") ?
                            recurringEvent.scheduledEvent().getName() : t;

                    String desc = (String) ctx.get("description");
                    desc = desc == null || desc.equalsIgnoreCase("NO") ?
                            recurringEvent.scheduledEvent().getDescription() : desc;

                    ScheduledEvent newEvent = scheduleNextEvent(guild, recurringEvent, scheduledEventListener, t, desc);
                    return InteractionResponses.messageAsEmbed("Successfully scheduled next event!", true);
                })
                .build();
    }

    private ScheduledEvent scheduleNextEvent(Guild guild, RecurringEvent recurringEvent,
                                   ScheduledEventListener scheduledEventListener,
                                   String newTitle, String newDesc) {

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

        ScheduledEvent newEvent = action.complete();
        // for some reason, the following 2 lines of code would execute out of order, hence the complete
        addNextToMap(scheduledEvent.getId(), newEvent.getId(),
                RecurringEvent.of(newEvent, recurringEvent.frequency()));
        return newEvent;
    }
}
