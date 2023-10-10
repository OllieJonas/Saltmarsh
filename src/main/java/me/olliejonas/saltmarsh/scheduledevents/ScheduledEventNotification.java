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
import java.util.Calendar;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Builder
public record ScheduledEventNotification(Member creator, String name, OffsetDateTime start,
                                         OffsetDateTime end, String description, ImageProxy image,
                                         String location, Set<String> interested, ScheduledEvent.Type type, ScheduledEvent.Status status) {

    public void addInterested(Member member) {
        interested.add(member.getAsMention());
    }

    public void removeInterested(Member member) {
        interested.remove(member.getAsMention());
    }

    public boolean isInterested(Member member) {
        return interested.contains(member.getAsMention());
    }

    public static ScheduledEventNotification fromEvent(ScheduledEvent event) {
        Set<String> interested = ConcurrentHashMap.newKeySet();
        event.retrieveInterestedMembers().forEachAsync(m -> interested.add(m.getAsMention())).join();

        return new ScheduledEventNotification(event.getGuild().retrieveMemberById(
                Objects.requireNonNull(event.getCreatorId())).complete(), event.getName(), event.getStartTime(),
                event.getEndTime(), event.getDescription(), event.getImage(), event.getLocation(), interested,
                event.getType(), event.getStatus());
    }

    public MessageEmbed toEmbed() {
        EmbedBuilder builder = EmbedUtils.colour();
        builder.setAuthor(getFormattedDate());
        builder.setTitle(name);
        builder.setDescription("Check out the Events tab to see more information and say you're coming!");

        String locationStr = type != ScheduledEvent.Type.EXTERNAL ? "<#" + location + ">" : location + " (in Person)";

        builder.addField("Organiser", creator.getAsMention(), true);
        builder.addField("Location", locationStr, true);
        builder.addField("Interested", String.join(", ", interested), true);
        builder.addField("Description", description, false);



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
        else
            endFormat.append("• ");

        endFormat.append("hh:mm a");
        DateTimeFormatter endDateFormatter = DateTimeFormatter.ofPattern(endFormat.toString());

        return start.format(startDateFormatter) + "  -  " + end.format(endDateFormatter);
    }
}
