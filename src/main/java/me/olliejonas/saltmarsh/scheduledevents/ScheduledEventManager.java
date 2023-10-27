package me.olliejonas.saltmarsh.scheduledevents;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import java.util.Optional;

public sealed interface ScheduledEventManager permits ScheduledEventManagerImpl {

    /**
     * Toggles the role to receive notifications for scheduled events.
     * If the role chosen is already the one designated to receive notifications, it will remove it.
     *
     * @return true if removed, false if added
     */
    boolean toggleRole(Guild guild, Role role);

    Optional<Role> getRole(Guild guild);

    void addCreator(ScheduledEvent event);

    /**
     * ONLY USE THIS AS A PURE CONVENIENCE / LAST RESORT, COMPLETE IS BAD
     */
    default Optional<Member> getCreator(Guild guild, ScheduledEvent event) {
        return getCreatorAction(guild, event).map(RestAction::complete);
    }

    Optional<CacheRestAction<Member>> getCreatorAction(Guild guild, ScheduledEvent event);

    /**
     * Toggles the channel to receive notifications for scheduled events.
     * If the channel chosen is already the one designated to receive notifications, it will remove it.
     *
     * @return true if removed, false if added
     */
    boolean toggleChannel(Guild guild, TextChannel channel);

    Optional<TextChannel> getChannel(Guild guild);

    void send(TextChannel channel, ScheduledEventNotification notification);

    void edit(TextChannel channel, ScheduledEventNotification notification);

    void delete(Guild guild, TextChannel channel, ScheduledEventNotification notification);
}
