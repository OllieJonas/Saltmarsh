package me.olliejonas.saltmarsh.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SkipCommand extends Command {

    private final AudioManager manager;

    public SkipCommand(AudioManager manager) {
        super(CommandPermissions.MUSIC, "skip");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(MUSIC) Skips a number of tracks in the queue");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.INTEGER, "skip", "The amount of tracks to skip (defaults to 1)")
        );
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        int skip = 1;

        if (args.containsKey("skip"))
            skip = args.get("skip").getAsInt();

        AudioTrack newTrack = manager.skip(executor.getGuild(), skip);

        return InteractionResponses.messageAsEmbed("Successfully skipped " + skip + " tracks! " +
                (newTrack == null ? "(Skipped to the end of the queue!)" :
                        "(Skipped to \"" + newTrack.getInfo().author + " - " + newTrack.getInfo().title + "\")"));
    }
}
