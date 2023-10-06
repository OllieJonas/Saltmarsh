package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class ResumeCommand extends AudioCommand {

    public ResumeCommand(GlobalAudioManager manager) {
        super(manager, "resume");
    }
    @Override
    public CommandInfo info() {
        return CommandInfo.of("Resumes playing of music! (-pause to pause the bot)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        manager.resume(channel.getGuild());
        return InteractionResponses.messageAsEmbed("Successfully resumed track!", true);
    }
}
