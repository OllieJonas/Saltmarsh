package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class DisconnectCommand extends AudioCommand {

    public DisconnectCommand(GlobalAudioManager manager) {
        super(manager, "disconnect", "dc", "quit", "exit");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Disconnects the bot from the voice channel!");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        manager.disconnect(executor.getGuild());
        return InteractionResponses.messageAsEmbed("Goodbye! <3");
    }
}
