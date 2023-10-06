package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestFailureCommand extends Command {

    public TestFailureCommand() {
        super(CommandPermissions.ADMIN, "failure", "fail");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of(
                "Testing that a CommandFailedException is triggered",
                "Throws a CommandFailedException (of reason OTHER), that confirms that it was an" +
                          "error message. Designed for usage in conjunction with Watchdog");
    }

    @Override
    public boolean shouldRegisterAsSlashCommand() {
        return false;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) {
        throw CommandFailedException.other("This message was sent as a result of a " +
                "CommandFailedException being thrown!", "");
    }
}
