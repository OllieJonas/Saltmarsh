package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ShuffleCommand extends AudioCommand {

    public ShuffleCommand(GlobalAudioManager manager) {
        super(manager, "shuffle");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Shuffles tracks in the queue! (NOT CURRENTLY IMPLEMENTED)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, Map<String, OptionMapping> args,
                                        String aliasUsed) throws CommandFailedException {
        manager.get(executor.getGuild());
        return InteractionResponses.messageAsEmbed("Command currently disabled!");
    }
}
