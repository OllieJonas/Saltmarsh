package me.olliejonas.saltmarsh.scheduledevents;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.ScheduledEvent;
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

    public ScheduledEventListener(ScheduledEventManager manager) {
        this.manager = manager;
    }

    private void onScheduledEvent(GenericScheduledEventGatewayEvent event, EventType type) {
        onScheduledEvent(event, type, event.getScheduledEvent().getStatus());
    }

    private void onScheduledEvent(GenericScheduledEventGatewayEvent event, EventType type, ScheduledEvent.Status status) {
        ScheduledEvent scheduledEvent = event.getScheduledEvent();

        if (manager.isEventUnregistered(scheduledEvent) && type != EventType.CREATE) return;

        ScheduledEventNotification notification = ScheduledEventNotification.fromEvent(scheduledEvent, status);
        MessageEmbed embed = notification.toEmbed();
        Role pingRole = manager.getRole(event.getGuild());

        BiConsumer<? super TextChannel, ? super Message> sendMessage = (channel, pingMessage) -> channel.sendMessageEmbeds(embed)
                .queue(success -> manager.registerMessage(scheduledEvent, channel, success, pingMessage));

        Consumer<? super TextChannel> action = switch (type) {
            case CREATE -> channel -> {
                if (pingRole != null) {
                    channel.sendMessage(pingRole.getAsMention() + " " +
                                    notification.creator().getEffectiveName() + " has made an event!")
                            .queue(ping -> sendMessage.accept(channel, ping));
                } else sendMessage.accept(channel, null);
            };
            case EDIT -> channel -> manager.callbackMessage(scheduledEvent, channel,
                    message -> message.editMessage(MessageEditData.fromEmbeds(embed)).queue());
            case DELETE -> channel -> {
                if (pingRole != null)
                    channel.sendMessage(pingRole.getAsMention() + " " + notification.name() + " has unfortunately been cancelled! :(").queue();
                manager.callbackMessage(scheduledEvent, channel, message -> message.editMessage(MessageEditData.fromEmbeds(embed)).queue());
            };
        };

        manager.getChannelsFor(event.getGuild())
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
