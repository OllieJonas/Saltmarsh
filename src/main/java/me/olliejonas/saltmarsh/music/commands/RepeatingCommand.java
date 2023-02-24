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

public class RepeatingCommand extends Command {

    private final GlobalAudioManager manager;

    public RepeatingCommand(GlobalAudioManager manager) {
        super("repeating", "togglerepeating", "repeat");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("Toggles whether the current track should repeat (indefinitely)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        GuildAudioManager guildAudioManager = Commons.from(manager, executor.getGuild());
        boolean repeating = guildAudioManager.repeating();
        return InteractionResponses.messageAsEmbed(repeating ? "Track is now repeating!" : "Track is no longer repeating!");
    }
}
