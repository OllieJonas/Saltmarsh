package me.olliejonas.saltmarsh.scheduledevents;

import lombok.Builder;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEvent;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Builder
public record ScheduledEventNotification(Member creator, String eventId, String name, OffsetDateTime start,
                                         OffsetDateTime end, String description, ImageProxy image,
                                         String location, Set<String> interested,
                                         ScheduledEvent.Type type, ScheduledEvent.Status status,
                                         String id, @Nullable RecurringEvent.Frequency frequency) {

    static final int AVATAR_SIZE = 32;
    static final int IMAGE_SIZE = 1024;

    static final int INTERESTED_LENGTH_THRESHOLD = 25;


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
        return fromEvent(event, event.getStatus(), null);
    }

    public static ScheduledEventNotification fromEvent(ScheduledEvent event, ScheduledEvent.Status status) {
        return fromEvent(event, status, null);
    }

    public static ScheduledEventNotification fromEvent(ScheduledEvent event, RecurringEventManager manager) {
        return fromEvent(event, event.getStatus(), manager);
    }

    public static ScheduledEventNotification fromEvent(ScheduledEvent event, ScheduledEvent.Status status,
                                                       RecurringEventManager manager) {
        return fromEvent(event, status, manager,
                event.getGuild().retrieveMemberById(
                        "140187632314351617").complete());
    }

    public static ScheduledEventNotification fromEvent(ScheduledEvent event, ScheduledEvent.Status status,
                                                       String creatorId, RecurringEventManager manager) {
        return fromEvent(event, status, manager,
                event.getGuild().retrieveMemberById(creatorId).complete());
    }

    // for some very, very stupid reason that I do not understand AT ALL, when deleting an event, the status passes
    // through as SCHEDULED, which breaks retrieving interested members, hence im specifying it up
    public static ScheduledEventNotification fromEvent(ScheduledEvent event, ScheduledEvent.Status status,
                                                       RecurringEventManager recurringEventManager, Member member) {
        // recurring
        RecurringEvent.Frequency frequency = null;

        if (recurringEventManager != null)
            frequency = recurringEventManager.get(event.getId())
                    .map(RecurringEvent::frequency).orElse(null);

        // interested
        Set<String> interested = ConcurrentHashMap.newKeySet();

        if (status != ScheduledEvent.Status.CANCELED && status != ScheduledEvent.Status.COMPLETED) {
            event.retrieveInterestedMembers().forEachAsync(m -> interested.add(m.getAsMention())).join();
        }


        OffsetDateTime startTime = event.getStartTime();
        OffsetDateTime endTime = event.getEndTime() == null ? null : event.getEndTime();

        ZoneId zoneId = ZoneId.of("Europe/London");
        ZoneOffset offset = startTime.getOffset();

        boolean isDST = offset.getTotalSeconds() != zoneId.getRules().getOffset(startTime.toInstant()).getTotalSeconds();

        startTime = isDST ? startTime.plusHours(1) : startTime;
        endTime = isDST ? endTime.plusHours(1) : endTime;

        return new ScheduledEventNotification(member, event.getId(), event.getName(), startTime,
                endTime, event.getDescription(), event.getImage(), event.getLocation(), interested,
                event.getType(), status, event.getId(), frequency);
    }

    public MessageEmbed toEmbed() {
        EmbedBuilder builder = EmbedUtils.colour();
        builder.setAuthor(getFormattedDate());
        builder.setTitle(isNoLongerAvailable() ? "~~" + name + "~~" : name);

        builder.setDescription(frequency == null ? STATUS_MESSAGES.get(status) :
                "This is a recurring event that repeats " + frequency.getRepresentation().toLowerCase(Locale.ROOT) + "!");

        String locationStr = type != ScheduledEvent.Type.EXTERNAL ? "<#" + location + ">" : location + " (in Person)";
        String interestedText = String.join(", ", interested);
        boolean shouldInterestedInline = interestedText.length() < INTERESTED_LENGTH_THRESHOLD;

        builder.addField("Organiser", creator.getAsMention(), true);
        builder.addField("Location", locationStr, true);
        builder.addField("Interested", String.join(", ", interested), shouldInterestedInline);
        builder.addField("Description", description, false);
        builder.addField("Status", statusStr(), false);

        ImageProxy avatar = MiscUtils.getMostRelevantAvatar(creator);

        if (image != null)
            builder.setImage(image.getUrl(IMAGE_SIZE));

        if (avatar != null)
            builder.setThumbnail(MiscUtils.getMostRelevantAvatar(creator).getUrl(AVATAR_SIZE));

        return builder.build();
    }

    public String getFormattedDate() {
        int startDay = start.getDayOfMonth();
        int startMonth = start.getMonthValue();
        int startYear = start.getYear();

        DateTimeFormatter startDateFormatter = DateTimeFormatter.ofPattern("EE MMM '" + MiscUtils.ordinal(startDay) +
                (Calendar.getInstance().get(Calendar.YEAR) - startYear == 0 ? "' " : "' yyyy ") + "• kk:mm a");

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

        endFormat.append("kk:mm a");
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
