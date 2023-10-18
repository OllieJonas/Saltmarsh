package me.olliejonas.saltmarsh.scheduledevents;

import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.scheduledevent.*;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScheduledEventListener extends ListenerAdapter {

    enum EventType {
        CREATE,
        EDIT,
        DELETE;

    }
    private final ScheduledEventManager manager;

    private final RecurringEventManager recurringEventManager;

    public ScheduledEventListener(ScheduledEventManager manager, RecurringEventManager recurringEventManager) {
        this.manager = manager;
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
//        ScheduledEvent scheduledEvent = event.getScheduledEvent();

        if (manager.isEventUnregistered(scheduledEvent) && type != EventType.CREATE) return;

        System.out.println("For " + type.name());
        ScheduledEventNotification notification = ScheduledEventNotification.fromEvent(scheduledEvent,
                status, recurringEventManager);

        MessageEmbed embed = notification.toEmbed();
        Role pingRole = manager.getRole(guild);

        BiConsumer<? super TextChannel, ? super Message> sendMessage = (channel, pingMessage) -> channel.sendMessageEmbeds(embed)
                .queue(success -> manager.registerMessage(scheduledEvent, channel, success, pingMessage));

        Consumer<? super TextChannel> action = switch (type) {
            case CREATE -> channel -> manager.registerAndSend(channel, scheduledEvent, recurringEventManager);
            case EDIT -> channel -> manager.callbackMessage(scheduledEvent, channel,
                    message -> message.editMessage(MessageEditData.fromEmbeds(embed)).queue());
            case DELETE -> channel -> {
                if (pingRole != null)
                    channel.sendMessage(pingRole.getAsMention() + " " + notification.name() + " has unfortunately been cancelled! :(").queue();
                manager.callbackMessage(scheduledEvent, channel, message -> message.editMessage(MessageEditData.fromEmbeds(embed)).queue());
            };
        };

        manager.getChannelsFor(guild)
                .forEach(action);
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

    // think jda forgot to include this one?
//    @Override
//    public void onScheduledEventUpdateImage(ScheduledEventUpdateImageEvent event) {
//        onScheduledEvent(event, EventType.EDIT);
//    }

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

        manager.removePingMessages(scheduledEvent);
        manager.destroyEvent(scheduledEvent);
    }

//    private void logScheduledEvent(ScheduledEvent event) {
//        System.out.println("Title: " + event.getName());
//        System.out.println("Organiser: " + (event.getCreator() == null ? "null" : event.getCreator().getName()));
//        System.out.println("Location: " + event.getLocation());
//        System.out.println("Description: " + event.getDescription());
//        System.out.println("Start Time: " + event.getStartTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
//        System.out.println("End Time: " + (event.getEndTime() == null ? "null" : event.getEndTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
//        System.out.println("Status: " + event.getStatus().name());
//        System.out.println("Type: " + event.getType().name());
//    }
}
