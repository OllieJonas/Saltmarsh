package me.olliejonas.saltmarsh.scheduledevents;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RegisterAlreadyExistingEvents extends ListenerAdapter {

    private final ScheduledEventManager scheduledEventManager;

    private final boolean enabled;

    public RegisterAlreadyExistingEvents(ScheduledEventManager scheduledEventManager) {
        this.scheduledEventManager = scheduledEventManager;
        this.enabled = false;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (enabled) {

        }
    }
}
