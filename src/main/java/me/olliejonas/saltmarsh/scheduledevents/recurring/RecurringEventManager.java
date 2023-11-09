package me.olliejonas.saltmarsh.scheduledevents.recurring;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

public sealed interface RecurringEventManager permits RecurringEventManagerImpl {

    void addChannel(Guild guild, TextChannel channel);

    Optional<RecurringEvent> get(String eventId);

    Optional<TextChannel> getChannel(Guild guild);

    boolean isRecurring(ScheduledEvent event);

    void register(RecurringEvent event, Guild guild);

    void remove(String eventId);

    default void update(RecurringEvent event, Guild guild) {
        register(event, guild);
    }
}
