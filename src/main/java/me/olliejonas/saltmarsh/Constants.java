package me.olliejonas.saltmarsh;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Constants {

    public static final String APP_TITLE = "Saltmarsh";

    public static final List<Color> APP_COLOURS = List.of(Color.CYAN, new Color(64, 224, 208));
    public static final CharSequence WATCHDOG_TITLE = "Watchdog";

    public static final Collection<String> COMMAND_PREFIXES = Collections.singleton("-");
    public static final String WATCHDOG_PREFIX = "[ WATCHDOG ] ";
    public static final String MINECRAFT_INDI_IP = "149.102.134.183:32465";

    public static String UNKNOWN_ERROR_PROMPT(String commandRoot, String message) {
        return String.format("An internal error has occurred! Please contact an admin! (Command: %s, " +
                "Error: %s)", commandRoot, message);
    }

    public static final Supplier<PaginatedEmbed> FAQ_EMBED = () -> PaginatedEmbed.standard()
            .textPage("Rationale", """
                    Recently, music-streaming bots such as Hydra have stopped supporting the scraping of audio data
                     from sites such as Spotify, YouTube, etc. for streaming via Discord. However, rather than
                     being done by taking measures to prevent these practises, this was done at the request of
                     said companies, who asked them to stop their services. Whilst Discord do offer an alternative
                     for streaming music, with them recently releasing their "Activities" feature which allows for
                     YouTube integration with the Discord client, in practise we have found it to be inconvenient.
                     Therefore, I have opted to write a Discord bot, replicating the same functionality as Hydra.
                    \s
                    For relatively obvious reasons, this bot can only be invited to guilds by its creator (ols#0001).
                     The source code is not available (again, obvious reasons), although you may be able to get a
                     compiled .jar file (this is written in Java, specifically using the JDA library (popular Java
                     bindings for Discord's REST API)) on my Jenkins server: https://ci.olliejonas.com. Just supply
                     the first argument for the program as your Discord bot token.
                    """)
            .page(EmbedUtils.standard()
                    .setTitle("FAQ (1)")
                    .addField("What's the prefix for commands to use this bot?", "-<command>", false)
                    .addField("What can this bot do?",
                            "Essentially, it's designed as a replacement for the music bot Hydra. " +
                                    "Most (non-premium) functionality has been replicated. " +
                                    "All commands are registered as slash commands, so you should be able to see " +
                                    "vaguely what this bot offers by typing \"/\".", false))
            .build();



    public static final List<Button> PAGINATED_EMBED_BUTTONS = Stream.of(Emoji.fromUnicode("⏮"),
            Emoji.fromUnicode("⏪"),
            Emoji.fromUnicode("⏩"),
            Emoji.fromUnicode("⏭"))

            .map(emoji -> Button.secondary("_", emoji)).toList();  // id is reset in paginatedembed anyways

    public static final List<String> MONTHS = List.of("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December");
}
