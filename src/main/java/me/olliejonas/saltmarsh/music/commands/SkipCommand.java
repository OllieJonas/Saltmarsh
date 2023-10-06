package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.GuildAudioManager;
import me.olliejonas.saltmarsh.music.exceptions.QueueException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class SkipCommand extends AudioCommand {

    private static final int MAX_SKIP_THRESHOLD = 200;

    public SkipCommand(GlobalAudioManager manager) {
        super(manager, "skip");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Skips the current track! (Specify a number to skip X tracks, e.g. -skip -5)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        int amount = 1;
        if (args.size() == 1) {
            try {
                amount = Math.max(1, Integer.parseInt(args.get(0)));
            } catch (NumberFormatException exception) {
                throw CommandFailedException.badArgs(executor, this, "tracks-to-skip (whole number)");
            }
        }

        if (amount >= MAX_SKIP_THRESHOLD) {
            sendMessage(channel, "Due to internal reasons, you are only able to skip a maximum of " +
                    MAX_SKIP_THRESHOLD + " tracks. Capping your input at this amount."
            );
            amount = MAX_SKIP_THRESHOLD;
        }


        GuildAudioManager guildAudioManager = from(manager, executor.getGuild());

        try {
            guildAudioManager.skip(amount);
        } catch (QueueException ex) {
            throw CommandFailedException.other(ex.getMessage(), ex.getMessage());
        }

        return InteractionResponses.messageAsEmbed("Successfully skipped " + amount + " track" +
                (amount == 1 ? "" : "s") + " !", true);
    }
}
