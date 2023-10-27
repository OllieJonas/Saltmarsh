package me.olliejonas.saltmarsh.scheduledevents.recurring;

import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;

public class Utils {

    static int BUTTON_MAX_LENGTH = 80;

    public static List<Button> fromEvents(List<ScheduledEvent> events, RecurringEventManager manager) {
        return events.stream()
                .filter(event -> manager == null || !manager.isRecurring(event))
                .map(event -> new Tuple2<>(event.getId(), event.getName()))
                .map(tuple -> tuple.map2(str -> str.length() >= BUTTON_MAX_LENGTH ? str.substring(0, BUTTON_MAX_LENGTH - 3) + "..." : str))
                .map(tuple -> Button.primary(tuple.v1(), tuple.v2())).toList();
    }
}
