package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.GuildAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResumeCommand extends Command {

    private final GlobalAudioManager manager;

    public ResumeCommand(GlobalAudioManager manager) {
        super("resume");
        this.manager = manager;
    }
    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        manager.resume(channel.getGuild());
        return InteractionResponses.messageAsEmbed("Successfully resumed track!", true);
    }
}
