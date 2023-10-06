package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.util.RandomUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class IsThisAURLCommand extends Command {


    public IsThisAURLCommand() {
        super("isthisaurl", "url");
    }

    @Override
    public List<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "url", "The url to check!"));
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Test whether something is a url!");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        if (args.size() != 1)
            throw CommandFailedException.badArgs(executor, this, "url");

        Optional<URL> urlOptional = RandomUtils.url(args.get(0));
        boolean isUrl = urlOptional.isPresent();

        StringBuilder builder = new StringBuilder("This is");

        if (!isUrl) builder.append(" not");

        builder.append(" a URL!");

        if (isUrl) {
            URL url = urlOptional.get();
            builder.append("\n");
            builder.append("Protocol: ").append(url.getProtocol()).append("\n");
        }

        return InteractionResponses.messageAsEmbed(builder.toString());
    }
}