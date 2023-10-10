package me.olliejonas.saltmarsh.scheduledevents.recurring;

import lombok.Getter;
import net.dv8tion.jda.api.entities.ScheduledEvent;

import java.time.OffsetDateTime;

public class RecurringEvent {
    enum Frequency {
        DAILY("Daily"),
        WEEKLY("Weekly"),
        BIWEEKLY("Bi-Weekly"),
        MONTHLY("Monthly");

        @Getter
        private final String representation;

        Frequency(String representation) {
            this.representation = representation;
        }
    }

    private final ScheduledEvent scheduledEvent;

    private final Frequency frequency;

    private OffsetDateTime nextDateTime;

    public static RecurringEvent of(ScheduledEvent scheduledEvent, Frequency frequency) {
        return new RecurringEvent(scheduledEvent, frequency);
    }

    public RecurringEvent(ScheduledEvent scheduledEvent, Frequency frequency) {
        this.scheduledEvent = scheduledEvent;
        this.frequency = frequency;
    }
}
