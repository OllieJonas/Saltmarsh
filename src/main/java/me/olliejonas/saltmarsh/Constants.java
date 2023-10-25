package me.olliejonas.saltmarsh;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Constants {

    public static class DB {
        public static final String RECURRING_EVENTS_META = "recurring_events_meta";
        public static final String RECURRING_EVENTS = "recurring_events";

        public static final String SCHEDULED_EVENTS_META = "scheduled_events_meta";

        public static final String SCHEDULED_EVENTS = "scheduled_events";
    }

    public static final String APP_TITLE = "Saltmarsh";

    public static final List<Color> APP_COLOURS = List.of(Color.CYAN, new Color(64, 224, 208));
    public static final CharSequence WATCHDOG_TITLE = "Watchdog";

    public static final Collection<String> COMMAND_PREFIXES = Collections.singleton("-");
    public static final String WATCHDOG_PREFIX = "[ WATCHDOG ] ";


    public static String UNKNOWN_ERROR_PROMPT(String commandRoot, String message) {
        return String.format("An internal error has occurred! Please contact an admin! (Command: %s, " +
                "Error: %s)", commandRoot, message);
    }

    public static final List<Button> PAGINATED_EMBED_BUTTONS = Stream.of(Emoji.fromUnicode("⏮"),
            Emoji.fromUnicode("⏪"),
            Emoji.fromUnicode("⏩"),
            Emoji.fromUnicode("⏭"))

            .map(emoji -> Button.secondary("_", emoji)).toList();  // id is reset in PaginatedEmbed anyway
}
