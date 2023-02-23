package me.olliejonas.saltmarsh.command.watchdog;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class AllocateWatchdogChannelCommand extends Command {

    private final CommandWatchdog watchdog;

    public AllocateWatchdogChannelCommand(CommandWatchdog watchdog) {
        super(CommandPermissions.ADMIN, "allocate-channel", "allocate", "alloc");
        this.watchdog = watchdog;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("", "");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        watchdog.allocateChannel(executor.getGuild(), channel);
        return InteractionResponses.messageAsEmbed(Constants.WATCHDOG_PREFIX + "Allocated this channel for watchdog related activity!");
    }
}
