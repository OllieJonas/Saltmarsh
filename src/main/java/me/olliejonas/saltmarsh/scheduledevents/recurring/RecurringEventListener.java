package me.olliejonas.saltmarsh.scheduledevents.recurring;

import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventListener;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventNotification;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.scheduledevent.ScheduledEventDeleteEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.ScheduledEventUpdateStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RecurringEventListener extends ListenerAdapter {

    private final RecurringEventManager manager;

    private final InputEmbedManager inputEmbedManager;

    private final ScheduledEventListener scheduledEventListener;

    public RecurringEventListener(InputEmbedManager inputEmbedManager, RecurringEventManager manager,
                                  ScheduledEventListener scheduledEventListener) {
        this.inputEmbedManager = inputEmbedManager;
        this.manager = manager;
        this.scheduledEventListener = scheduledEventListener;
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

        TextChannel channel = manager.getTextChannelForRecurringDump(guild);
        if (channel == null) return;

        if (!manager.isRecurring(event)) return;

        channel.sendMessage("@here A recurring event has " +
                (reason == ScheduledEvent.Status.COMPLETED ? "finished!" : "been cancelled!")).submit()
                .thenCompose(__ -> guild.retrieveMemberById(Objects.requireNonNull(event.getCreatorId())).submit())
                .thenCompose(member -> channel.sendMessageEmbeds(
                        ScheduledEventNotification.fromEvent(event, reason, manager, member).toEmbed()).submit())
                .whenComplete((msg, error) -> {
                    if (error != null) error.printStackTrace();

                    manager.sendUpdateEventEmbed(inputEmbedManager,
                            scheduledEventListener, guild, event, reason).queue(null, channel);
                });
    }
}
