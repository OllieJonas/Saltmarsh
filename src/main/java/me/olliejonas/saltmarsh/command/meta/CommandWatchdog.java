package me.olliejonas.saltmarsh.command.meta;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbed;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandWatchdog {

    @Getter
    public static class Report {

        enum Result {
            SUCCESS(Color.GREEN),
            FAILURE(Color.RED);

            @Getter
            private final Color colour;

            Result(Color colour) {
                this.colour = colour;
            }
        }

        @Setter
        private Member executor;

        private final Result result;

        private final LocalDateTime executionTime;

        @Setter
        private String aliasUsed;

        @Setter
        private String messageRaw;


        private final TextChannel channel;

        private final Throwable failedException;

        public Report(Throwable exception, Member executor, TextChannel channel, String alias, String messageRaw) {
            this(exception == null ? Result.SUCCESS : Result.FAILURE, alias, messageRaw, executor, channel, exception);
        }


        private Report(Result result, String aliasUsed, String messageRaw,
                       Member executor, TextChannel channel, Throwable failedException) {
            this.result = result;
            this.aliasUsed = aliasUsed;
            this.messageRaw = messageRaw;
            this.executor = executor;
            this.channel = channel;
            this.failedException = failedException;
            this.executionTime = LocalDateTime.now();
        }

        public boolean failed() {
            return this.result == Result.FAILURE;
        }

        public ButtonEmbed asEmbed() {
            EmbedBuilder embedBuilder = EmbedUtils.essentials();
            ButtonEmbed.Builder buttonBuilder = null;

            embedBuilder.setTitle("Watchdog Report");
            embedBuilder.setColor(result.getColour());

            embedBuilder.addField("Command", executor.getEffectiveName() + " executed " + messageRaw, false);
            embedBuilder.addField("Text Channel", channel.getName(), false);
            embedBuilder.addField("Status", result.name(), false);

            if (failed()) {
                embedBuilder.addField("Error Message", "" + failedException.getLocalizedMessage(), false);

                if (failedException instanceof CommandFailedException commandFailedException) {
                    embedBuilder.addField("Reason", commandFailedException.getReason().name(), false);
                    embedBuilder.addField("Context", commandFailedException.getContext(), false);
                }

                embedBuilder.addField("", "React to the ❓ to get the stack trace", false);

                buttonBuilder = ButtonEmbed.builder(embedBuilder).button("❓", context -> {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final String utf8 = StandardCharsets.UTF_8.name();

                    try (PrintStream ps = new PrintStream(baos, true, utf8)) {
                        failedException.printStackTrace(ps);
                        String data = baos.toString(utf8);
                        System.out.println(Constants.WATCHDOG_PREFIX + context.clicker().getEffectiveName() +
                                " has requested the stack trace for a command they executed:");
                        failedException.printStackTrace();
                        return InteractionResponses.messageAsEmbed(data, true);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });

            }


            return buttonBuilder == null ? ButtonEmbed.builder(embedBuilder.build()).build() : buttonBuilder.build();
        }
    }

    private final ButtonEmbedManager manager;
    private final Map<String, TextChannel> watchdogAllocatedChannels;

    private final Set<String> guildsToggledWatchdog;


    public CommandWatchdog(ButtonEmbedManager manager) {
        this.manager = manager;
        this.watchdogAllocatedChannels = new HashMap<>();
        this.guildsToggledWatchdog = new HashSet<>();
    }

    public boolean toggleWatchdog(Guild guild) {
        String id = guild.getId();

        boolean status = guildsToggledWatchdog.contains(id);
        if (status)
            guildsToggledWatchdog.remove(id);
        else
            guildsToggledWatchdog.add(id);

        return !status;
    }

    public void allocateChannel(Guild guild, TextChannel channel) {
        watchdogAllocatedChannels.put(guild.getId(), channel);
        toggleWatchdog(guild);
    }

    public void report(Report result) {
        String id = result.getExecutor().getGuild().getId();
        if (guildsToggledWatchdog.contains(id)) {
            if (watchdogAllocatedChannels.containsKey(id)) {
                manager.send(watchdogAllocatedChannels.get(id), result.asEmbed());
            }
        }
    }
}
