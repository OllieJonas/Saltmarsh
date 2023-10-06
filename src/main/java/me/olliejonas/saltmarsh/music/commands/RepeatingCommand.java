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

public class RepeatingCommand extends AudioCommand {

    public RepeatingCommand(GlobalAudioManager manager) {
        super(manager, "repeating", "togglerepeating", "repeat");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Toggles whether the current track should repeat (indefinitely)");
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, Map<String, OptionMapping> args,
                                        String aliasUsed) throws CommandFailedException {
        GuildAudioManager guildAudioManager = from(manager, executor.getGuild());
        boolean repeating = guildAudioManager.repeating();
        return InteractionResponses.messageAsEmbed(repeating ? "Track is now repeating!" : "Track is no longer repeating!");
    }
}
