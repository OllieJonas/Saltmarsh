package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.commands.Commons;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class TestYTSearchCommand extends Command {

    private final GlobalAudioManager manager;

    public TestYTSearchCommand(GlobalAudioManager manager) {
        super(CommandPermissions.ADMIN, "ytsearch");
        this.manager = manager;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        return Commons.joinAndPlay(manager, channel, executor, "ytsearch:Road work ahead? Uh yeah, I sure hope it does.");
    }
}
