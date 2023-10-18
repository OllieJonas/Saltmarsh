package me.olliejonas.saltmarsh.scheduledevents.recurring;

import lombok.Getter;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Locale;

public record RecurringEvent(ScheduledEvent scheduledEvent, Frequency frequency) {
    public enum Frequency {
        DAILY("Daily", Period.ofDays(1)),
        WEEKLY("Weekly", Period.ofWeeks(1)),
        BIWEEKLY("Bi-Weekly", Period.ofWeeks(2)),
        MONTHLY("Monthly", Period.ofMonths(1));

        @Getter
        private final String representation;

        private final Period offset;

        public static Frequency from(String representation) {
            return switch(representation.toLowerCase(Locale.ROOT)) {
                case "daily" -> DAILY;
                case "weekly" -> WEEKLY;
                case "bi-weekly", "biweekly" -> BIWEEKLY;
                case "monthly" -> MONTHLY;
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

    public static RecurringEvent of(ScheduledEvent scheduledEvent, Frequency frequency) {
        return new RecurringEvent(scheduledEvent, frequency);
    }
}
