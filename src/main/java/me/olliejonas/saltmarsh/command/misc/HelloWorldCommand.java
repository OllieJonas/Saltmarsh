package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class HelloWorldCommand extends Command {

    public HelloWorldCommand() {
        super(CommandPermissions.ALL, "helloworld", "hello", "ping", "args");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Hello, world!", "N/A");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel,
                                        Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return switch (aliasUsed) {
            case "helloworld", "hello" -> helloWorld(executor, channel);
            case "ping" -> pong(channel);
            case "args" -> args(channel, args);

            default -> throw CommandFailedException.saltmashInternal(
                    "switch case reached default clause on helloworld command (shouldn't see this)");
        };
    }

    private InteractionResponses pong(TextChannel channel) {
        return InteractionResponses.messageAsEmbed("pong!");
    }

    private InteractionResponses helloWorld(Member executor, TextChannel channel) {
        String message = String.format("Hello, %s!", executor.getEffectiveName());
        return InteractionResponses.messageAsEmbed(message);
    }

    private InteractionResponses args(TextChannel channel, Map<String, OptionMapping> args) {
        String message = String.format("args: %s", String.join(", ", args.keySet()));
        return InteractionResponses.messageAsEmbed(message);
    }
}
