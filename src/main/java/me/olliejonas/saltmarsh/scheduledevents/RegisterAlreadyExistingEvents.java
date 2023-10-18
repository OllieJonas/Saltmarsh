package me.olliejonas.saltmarsh.scheduledevents;

import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class RegisterAlreadyExistingEvents extends ListenerAdapter {

    private final ScheduledEventManager scheduledEventManager;

    private final RecurringEventManager recurringEventManager;

    private final boolean enabled;

    public RegisterAlreadyExistingEvents(ScheduledEventManager scheduledEventManager, RecurringEventManager recurringEventManager) {
        this.scheduledEventManager = scheduledEventManager;
        this.recurringEventManager = recurringEventManager;
        this.enabled = false;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (enabled) {

            List<ScheduledEvent> unregisteredEvents = event.getGuild().getScheduledEvents().stream()
                    .filter(e -> e.getId().equals(""))
                    .toList();

            scheduledEventManager.getChannelsFor(event.getGuild())
                    .forEach(ch -> unregisteredEvents.forEach(ev ->
                            scheduledEventManager.registerAndSend(ch, ev, recurringEventManager)));
        }
    }
}
