package me.olliejonas.saltmarsh.scheduledevents.recurring;

import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventNotification;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventDeleteEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class RecurringEventListener extends ListenerAdapter {

    private final RecurringEventManagerImpl manager;

    public RecurringEventListener(RecurringEventManager manager) {
        this.manager = (RecurringEventManagerImpl) manager;
    }

    @Override
    public void onScheduledEventDelete(@NotNull ScheduledEventDeleteEvent event) {
        onRecurringEventExpiring(event.getGuild(), event.getScheduledEvent(), ScheduledEvent.Status.CANCELED);
    }

    @Override
    public void onScheduledEventUpdateStatus(@NotNull ScheduledEventUpdateStatusEvent event) {
        onRecurringEventExpiring(event.getGuild(), event.getScheduledEvent(), event.getNewStatus());
    }

    private void onRecurringEventExpiring(Guild guild, ScheduledEvent event, ScheduledEvent.Status reason) {
        if (reason != ScheduledEvent.Status.CANCELED && reason != ScheduledEvent.Status.COMPLETED) return;
        if (!manager.isRecurring(event)) return;

        RecurringEvent recurringEvent = manager.get(event.getId()).orElseThrow();

        Optional<TextChannel> channelOpt = manager.getChannel(guild);

        if (channelOpt.isEmpty()) {
            manager.scheduleNextEvent(guild, recurringEvent, null, null);
            return;
        }

        TextChannel channel = channelOpt.get();

        channel.sendMessage("@here A recurring event has " +
                (reason == ScheduledEvent.Status.COMPLETED ? "finished!" : "been cancelled!")).submit()
                .thenCompose(__ -> guild.retrieveMemberById(Objects.requireNonNull(recurringEvent.creator().getId())).submit())
                .thenCompose(member -> channel.sendMessageEmbeds(
                        ScheduledEventNotification.fromEvent(event, reason, manager, member).toEmbed(false)).submit())
                .whenComplete((msg, error) -> {
                    if (error != null) error.printStackTrace();

                    manager.sendUpdateEventEmbed(guild, event, reason).queue(null, channel);
                });
    }
}
