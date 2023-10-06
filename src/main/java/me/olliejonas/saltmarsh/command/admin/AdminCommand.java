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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AdminCommand extends Command {

    private final GlobalAudioManager globalAudioManager;

    @Override
    public void addSubCommands() {
        addSubCommand(new AdminClearCacheCommand(globalAudioManager));
    }

    public AdminCommand(GlobalAudioManager globalAudioManager) {
        super(CommandPermissions.ADMIN, "admin");
        this.globalAudioManager = globalAudioManager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Various misc admin commands (ADMIN)");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "subcommand", "The subcommand!"));
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed("help");
    }
}
