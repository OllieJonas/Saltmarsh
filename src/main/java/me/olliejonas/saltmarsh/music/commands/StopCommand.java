package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class StopCommand extends Command {

    private final AudioManager manager;

    public StopCommand(AudioManager manager) {
        super(CommandPermissions.MUSIC, "stop");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(MUSIC) Stops the current track");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        boolean stopped = manager.stop(executor.getGuild());
        return InteractionResponses.messageAsEmbed(stopped ? "Successfully stopped track!" : "Unable to stop track!");
    }
}
