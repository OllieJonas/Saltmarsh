package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class DisconnectCommand extends Command {

    private final AudioManager manager;

    public DisconnectCommand(AudioManager manager) {
        super(CommandPermissions.MUSIC, "disconnect", "dc");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(MUSIC) Disconnects the bot from the channel");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        boolean disconnected = manager.disconnect(executor.getGuild());
        return InteractionResponses.messageAsEmbed(disconnected ? "Goodbye! <3" : "I'm not currently in a voice channel!");
    }
}
