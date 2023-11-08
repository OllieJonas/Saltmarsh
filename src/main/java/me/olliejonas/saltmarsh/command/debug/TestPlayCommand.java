package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestPlayCommand extends Command {

    private final AudioManager manager;

    public TestPlayCommand(AudioManager manager) {
        super(CommandPermissions.ADMIN, "play");
        this.manager = manager;
    }

    static final String TOM_SCOTT_ONE = "https://www.youtube.com/watch?v=j3OqAN4ISOw";

    static final String TOM_SCOTT_TWO = "https://www.youtube.com/watch?v=CmZdGo6b5yA";
    static final String BONK = "https://www.youtube.com/watch?v=6TP0abZdRXg";

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        manager.playTrack(executor, TOM_SCOTT_ONE);
        manager.playTrack(executor, BONK);
        manager.playTrack(executor, TOM_SCOTT_TWO);

        return InteractionResponses.empty();
    }
}
