package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SayInAnEchoingVoiceCommand extends Command {

    public SayInAnEchoingVoiceCommand() {
        super(CommandPermissions.ADMIN, "sayinanechoingvoice", "say-in-an-echoing-voice");
    }
    @Override
    public CommandInfo info() {
        return CommandInfo.of("Send messages as embed (ADMIN)");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "text", "what text to say in a booming voice", true));
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed(args.get("text").getAsString());
    }
}
