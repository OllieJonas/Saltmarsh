package me.olliejonas.saltmarsh.command.watchdog;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class ToggleWatchdogCommand extends Command {

    private final CommandWatchdog watchdog;

    public ToggleWatchdogCommand(CommandWatchdog watchdog) {
        super(CommandPermissions.ADMIN, "toggle", "enable", "disable", "toggle-reports");
        this.watchdog = watchdog;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Toggles Watchdog", "Enabled/Disable also use this same thing...");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        boolean status = watchdog.toggleWatchdog(executor.getGuild());
        String message = String.format(Constants.WATCHDOG_PREFIX + "Watchdog is now %s!", status ? "enabled" : "disabled");
        return InteractionResponses.messageAsEmbed(message);
    }
}
