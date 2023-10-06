package me.olliejonas.saltmarsh.command.watchdog;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WatchdogCommand extends Command {

    private final CommandWatchdog watchdog;

    public WatchdogCommand(@NotNull CommandWatchdog watchdog) {
        super(CommandPermissions.ADMIN, "watchdog", "wd");
        this.watchdog = watchdog;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of(
                "All watchdog related commands.",
                "Logs all commands recognised by Saltmarsh into a designated error channel.");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        return help();
    }

    @Override
    public void addSubCommands() {
        addSubCommand(new AllocateWatchdogChannelCommand(watchdog));
        addSubCommand(new ToggleWatchdogCommand(watchdog));
    }
}
