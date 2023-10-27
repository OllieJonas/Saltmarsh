package me.olliejonas.saltmarsh.scheduledevents.recurring;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Locale;

public record RecurringEvent(ScheduledEvent scheduledEvent, Member creator, Frequency frequency) {

    public enum Frequency {
        DAILY("Daily", Period.ofDays(1)),
        WEEKLY("Weekly", Period.ofWeeks(1)),
        BIWEEKLY("Bi-Weekly", Period.ofWeeks(2)),
        MONTHLY("Monthly", Period.ofMonths(1));

        @Getter
        private final String representation;

        private final Period offset;

        public static Frequency from(String representation) {
            return switch(representation.toUpperCase(Locale.ROOT)) {
                case "DAILY" -> DAILY;
                case "WEEKLY" -> WEEKLY;
                case "BI-WEEKLY", "BIWEEKLY" -> BIWEEKLY;
                case "MONTHLY" -> MONTHLY;
                default -> throw new IllegalStateException("Unexpected value: " + representation);
            };
        }

        Frequency(String representation, Period offset) {
            this.representation = representation;
            this.offset = offset;
        }

        public OffsetDateTime next(@Nullable OffsetDateTime curr) {
            if (curr == null) return null;

            return curr.plus(offset);
        }
    }

    public static RecurringEvent from(JDA jda, String guildId, String eventId, String memberId, String frequency) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) throw new IllegalStateException("guild cannot be null when creating RecurringEvent!");

        return from(guild, memberId, eventId, frequency);
    }

    public static RecurringEvent from(Guild guild, String creatorId, String eventId, String frequency) {
        Member creator = guild.retrieveMemberById(creatorId).complete();
        return new RecurringEvent(guild.getScheduledEventById(eventId), creator, Frequency.from(frequency));
    }

    public static RecurringEvent of(ScheduledEvent scheduledEvent, Member creator, Frequency frequency) {
        return new RecurringEvent(scheduledEvent, creator, frequency);
    }
}
