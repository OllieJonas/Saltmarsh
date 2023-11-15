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

public class ClearQueueCommand extends Command {

    private final AudioManager manager;

    public ClearQueueCommand(AudioManager manager) {
        super(CommandPermissions.MUSIC, "clear-queue");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(MUSIC) Clears the queue");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed("Removed " + manager.clearQueue(executor.getGuild()) + " items from the queue!");
    }
}
