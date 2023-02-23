package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class TestFailureCommand extends Command {

    public TestFailureCommand() {
        super(CommandPermissions.ADMIN, "failure", "fail");
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of(
                "Testing that a CommandFailedException is triggered",
                "Throws a CommandFailedException (of reason OTHER), that confirms that it was an" +
                          "error message. Designed for usage in conjunction with Watchdog");
    }

    @Override
    public boolean registerAsSlashCommand() {
        return false;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) {
        throw CommandFailedException.other("This message was sent as a result of a " +
                "CommandFailedException being thrown!", "");
    }
}
