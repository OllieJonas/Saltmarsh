package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SayInAnEchoingVoiceCommand extends Command {

    public SayInAnEchoingVoiceCommand() {
        super(CommandPermissions.ADMIN, "sayinanechoingvoice", "say-in-an-echoing-voice");
    }
    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed(String.join(" ", args));
    }
}
