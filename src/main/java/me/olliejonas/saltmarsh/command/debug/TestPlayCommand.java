package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.commands.Commons;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class TestPlayCommand extends Command {

    private final GlobalAudioManager manager;

    public TestPlayCommand(GlobalAudioManager manager) {
        super(CommandPermissions.ADMIN, "play");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        return Commons.joinAndPlay(manager, channel,
                executor, "https://www.youtube.com/watch?v=X6NJkWbM1xk&ab_channel=TomScott"); // the irony is killing me
    }
}
