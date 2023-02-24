package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class ShuffleCommand extends Command {

    private final GlobalAudioManager manager;

    public ShuffleCommand(GlobalAudioManager manager) {
        super("shuffle");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("Shuffles tracks in the queue! (NOT CURRENTLY IMPLEMENTED)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        manager.get(executor.getGuild());
        return InteractionResponses.messageAsEmbed("Command currently disabled!");
    }
}
