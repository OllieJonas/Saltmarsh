package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.GuildAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class PauseCommand extends Command {

    private final GlobalAudioManager manager;

    public PauseCommand(GlobalAudioManager manager) {
        super("pause");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("Pauses the music-bot, if it's playing! (Resume with -resume)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        GuildAudioManager guildAudioManager = Commons.from(manager, executor.getGuild());
        guildAudioManager.pause();
        return InteractionResponses.messageAsEmbed("Successfully paused!", true);
    }
}
