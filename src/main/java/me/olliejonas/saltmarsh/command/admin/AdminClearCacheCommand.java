package me.olliejonas.saltmarsh.command.admin;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class AdminClearCacheCommand extends Command {

    private final GlobalAudioManager manager;

    public AdminClearCacheCommand(GlobalAudioManager manager) {
        super(CommandPermissions.ADMIN, "clear-cache", "clearcache");

        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {

        if (!manager.remove(executor.getGuild()))
            throw CommandFailedException.other("Unable to find guild!",
                    "bro you're the one who wrote this you know EXACTLY why this mf error is coming up");

        return InteractionResponses.messageAsEmbed("Successfully cleared cache!");
    }
}
