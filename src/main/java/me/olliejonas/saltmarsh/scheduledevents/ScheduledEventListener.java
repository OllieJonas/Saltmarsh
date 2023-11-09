package me.olliejonas.saltmarsh.scheduledevents;

import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.*;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import java.util.Optional;

public class ScheduledEventListener extends ListenerAdapter {

    enum EventType {
        CREATE,
        EDIT,
        DELETE;
    }

    private final ScheduledEventManagerImpl manager;

    private final RecurringEventManager recurringEventManager;

    public ScheduledEventListener(ScheduledEventManager manager, RecurringEventManager recurringEventManager) {
        this.manager = (ScheduledEventManagerImpl) manager;
        this.recurringEventManager = recurringEventManager;
    }

    private void onScheduledEvent(GenericScheduledEventGatewayEvent event, EventType type) {
        onScheduledEvent(event.getGuild(), event.getScheduledEvent(), type);
    }

    private void onScheduledEvent(Guild guild, ScheduledEvent event, EventType type) {
        onScheduledEvent(guild, event, type, event.getStatus());
    }

    private void onScheduledEvent(GenericScheduledEventGatewayEvent event, EventType type, ScheduledEvent.Status status) {
        onScheduledEvent(event.getGuild(), event.getScheduledEvent(), type, status);
    }

    private void onScheduledEvent(Guild guild, ScheduledEvent scheduledEvent,
                                  EventType type, ScheduledEvent.Status status) {

        if (manager.isEventUnregistered(scheduledEvent) && type != EventType.CREATE) return;

        if (type == EventType.CREATE)
            manager.addCreator(scheduledEvent);

        Optional<CacheRestAction<Member>> memberAction = manager.getCreatorAction(guild, scheduledEvent);

        if (memberAction.isEmpty()) return;

        memberAction.get().submit()
                .thenApply(creator -> ScheduledEventNotification.fromEvent(
                        scheduledEvent, status, recurringEventManager, creator))

                .whenComplete((notification, ex) -> {
                    Optional<Role> roleOpt = manager.getRole(guild);
                    if (ex != null) throw new RuntimeException(ex);

                    manager.getChannel(guild)
                            .ifPresent(channel -> {
                                switch (type) {
                                    case CREATE -> manager.send(channel, notification);
                                    case EDIT -> manager.edit(channel, notification);
                                    case DELETE -> manager.delete(guild, channel, notification);
                                }
                            });
                });
    }

    // -------- create --------

    @Override
    public void onScheduledEventCreate(ScheduledEventCreateEvent event) {
        onScheduledEvent(event, EventType.CREATE);
    }

    // -------- users --------

    @Override
    public void onScheduledEventUserAdd(ScheduledEventUserAddEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }
    @Override
    public void onScheduledEventUserRemove(ScheduledEventUserRemoveEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    //-------- edit --------

    public void onScheduledEventUpdateRecurring(Guild guild, ScheduledEvent event) {
        onScheduledEvent(guild, event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateDescription(ScheduledEventUpdateDescriptionEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateEndTime(ScheduledEventUpdateEndTimeEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateImage(ScheduledEventUpdateImageEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateLocation(ScheduledEventUpdateLocationEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateName(ScheduledEventUpdateNameEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateStartTime(ScheduledEventUpdateStartTimeEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    @Override
    public void onScheduledEventUpdateStatus(ScheduledEventUpdateStatusEvent event) {
        onScheduledEvent(event, EventType.EDIT);
    }

    // -------- delete --------
    @Override
    public void onScheduledEventDelete(ScheduledEventDeleteEvent event) {
        ScheduledEvent scheduledEvent = event.getScheduledEvent();
//        logScheduledEvent(scheduledEvent);
        onScheduledEvent(event, EventType.DELETE, ScheduledEvent.Status.CANCELED);
//        manager.getChannelsFor(event.getGuild())
//                .forEach(channel -> manager.callbackMessage(scheduledEvent, channel,
//                        message -> message.editMessage(MessageEditData.fromEmbeds()).queue()));

        if (manager.isEventUnregistered(scheduledEvent)) return;
        manager.destroy(scheduledEvent);
    }
}
