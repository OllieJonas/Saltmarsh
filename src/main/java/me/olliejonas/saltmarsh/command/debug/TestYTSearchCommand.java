package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.commands.AudioCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestYTSearchCommand extends AudioCommand {

    public TestYTSearchCommand(GlobalAudioManager manager) {
        super(manager, "ytsearch");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return joinAndPlay(manager, channel, executor, "ytsearch:Road work ahead? Uh yeah, I sure hope it does.");
    }
}
