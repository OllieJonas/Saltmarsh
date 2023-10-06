package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.GuildAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class StopCommand extends AudioCommand {

    public StopCommand(GlobalAudioManager manager) {
        super(manager, "stop");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Stops whatever is currently playing!");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, Map<String, OptionMapping> args,
                                        String aliasUsed) throws CommandFailedException {
        GuildAudioManager guildManager = from(manager, executor.getGuild());
        guildManager.stop();
        return InteractionResponses.messageAsEmbed("Successfully stopped queue!", true);
    }
}
