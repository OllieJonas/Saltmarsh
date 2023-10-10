package me.olliejonas.saltmarsh.scheduledevents;

import lombok.Builder;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.utils.ImageProxy;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Builder
public record ScheduledEventNotification(Member creator, String name, OffsetDateTime start,
                                         OffsetDateTime end, String description, ImageProxy image,
                                         String location, Set<String> interested,
                                         ScheduledEvent.Type type, ScheduledEvent.Status status) {

    public static Map<ScheduledEvent.Status, String> STATUS_MESSAGES = Map.of(
            ScheduledEvent.Status.SCHEDULED, "Check out the Events tab to see " +
                    "more information and say you're coming!",
            ScheduledEvent.Status.ACTIVE, "This event is currently underway!",
            ScheduledEvent.Status.COMPLETED, "This event has already happened! :(",
            ScheduledEvent.Status.CANCELED, "This event has been cancelled!"
    );

    public boolean isInterested(Member member) {
        return interested.contains(member.getAsMention());
    }

    public boolean isNoLongerAvailable() {
        return status == ScheduledEvent.Status.CANCELED || status == ScheduledEvent.Status.COMPLETED;
    }

    public static ScheduledEventNotification fromEvent(ScheduledEvent event) {
        return fromEvent(event, event.getStatus());
    }

    // for some very, very stupid reason that I do not understand AT ALL, when deleting an event, the status passes
    // through as SCHEDULED, which breaks retrieving interested members, hence im specifying it up
    public static ScheduledEventNotification fromEvent(ScheduledEvent event, ScheduledEvent.Status status) {
        Set<String> interested = ConcurrentHashMap.newKeySet();

        if (status != ScheduledEvent.Status.CANCELED && status != ScheduledEvent.Status.COMPLETED) {
            event.retrieveInterestedMembers().forEachAsync(m -> interested.add(m.getAsMention())).join();
        }

        OffsetDateTime endTime = event.getEndTime() == null ? null : event.getEndTime().plus(1, ChronoUnit.HOURS);

        return new ScheduledEventNotification(event.getGuild().retrieveMemberById(
                Objects.requireNonNull(event.getCreatorId())).complete(), event.getName(), event.getStartTime()
                .plus(1, ChronoUnit.HOURS),
                endTime, event.getDescription(), event.getImage(), event.getLocation(), interested,
                event.getType(), status);
    }

    public MessageEmbed toEmbed() {
        EmbedBuilder builder = EmbedUtils.colour();
        builder.setAuthor(getFormattedDate());
        builder.setTitle(isNoLongerAvailable() ? "~~" + name + "~~" : name);

        builder.setDescription(STATUS_MESSAGES.get(status));

        String locationStr = type != ScheduledEvent.Type.EXTERNAL ? "<#" + location + ">" : location + " (in Person)";

        builder.addField("Organiser", creator.getAsMention(), true);
        builder.addField("Location", locationStr, true);
        builder.addField("Interested", String.join(", ", interested), true);
        builder.addField("Description", description, false);
        builder.addField("Status", statusStr(), false);



        ImageProxy avatar = MiscUtils.getMostRelevantAvatar(creator);

        if (image != null)
            builder.setThumbnail(image.getUrl());

//        if (avatar != null)
//            builder.setImage(avatar.getUrl());

        return builder.build();
    }

    public String getFormattedDate() {
        int startDay = start.getDayOfMonth();
        int startMonth = start.getMonthValue();
        int startYear = start.getYear();

        DateTimeFormatter startDateFormatter = DateTimeFormatter.ofPattern("EE MMM '" + MiscUtils.ordinal(startDay) +
                (Calendar.getInstance().get(Calendar.YEAR) - startYear == 0 ? "' " : "' yyyy ") + "• hh:mm a");

        if (end == null || start == end)
            return start.format(startDateFormatter);

        int endDay = end.getDayOfMonth();
        int endYear = end.getYear();

        StringBuilder endFormat = new StringBuilder();
        if (endDay != startDay)
            endFormat.append("EE MMM '").append(MiscUtils.ordinal(endDay)).append("' ");

        if (endYear != startYear)
            endFormat.append("yyyy • ");
        else if (endDay != startDay)
            endFormat.append("• ");

        endFormat.append("hh:mm a");
        DateTimeFormatter endDateFormatter = DateTimeFormatter.ofPattern(endFormat.toString());

        return start.format(startDateFormatter) + "  -  " + end.format(endDateFormatter);
    }

    private String statusStr() {
        return switch (status) {
            case SCHEDULED -> "Scheduled";
            case CANCELED -> "Cancelled";
            case ACTIVE -> "Active";
            case COMPLETED -> "Completed";
            case UNKNOWN -> "Unknown";
        };
    }
}
