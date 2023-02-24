package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class SayInAnEchoingVoiceCommand extends Command {

    public SayInAnEchoingVoiceCommand() {
        super(CommandPermissions.ADMIN, "sayinanechoingvoice", "say-in-an-echoing-voice");
    }
    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("Send messages as embed (ADMIN)");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed(String.join(" ", args));
    }
}
