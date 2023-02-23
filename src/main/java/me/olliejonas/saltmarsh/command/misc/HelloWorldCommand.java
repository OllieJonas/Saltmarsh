package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class HelloWorldCommand extends Command {

    public HelloWorldCommand() {
        super(CommandPermissions.ALL, "helloworld", "hello", "ping", "args");
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("Hello, world!", "N/A");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel,
                                        List<String> args, String aliasUsed) throws CommandFailedException {
        return switch (aliasUsed) {
            case "helloworld", "hello" -> helloWorld(executor, channel);
            case "ping" -> pong(channel);
            case "args" -> args(channel, args);

            default -> throw CommandFailedException.saltmashInternal(
                    "switch case reached default clause on helloworld command (shouldn't see this)");
        };
    }

    @Override
    public void addSubCommands() {
        // could implement this elsewhere, but here we are
        addSubCommand(new Command("subcommand") {
            @Override
            public CommandInfo commandInfo() {
                return CommandInfo.of("Simple subcommand to test usage", "N/A");
            }

            @Override
            public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
                String message = String.format("You executed a subcommand! Args: %s", String.join(", ", args));
                return InteractionResponses.messageAsEmbed(message);
            }

            @Override
            public void addSubCommands() {
                addSubCommand(new Command("subsub") {
                    @Override
                    public CommandInfo commandInfo() {
                        return CommandInfo.of("goin reaaaal deep now", "oh yeah baby");
                    }

                    @Override
                    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
                        String message = "wow you've gone reeeeal deep now... args: " + String.join(", ", args);
                        return InteractionResponses.messageAsEmbed(message);
                    }
                });
            }
        });
    }

    private InteractionResponses pong(TextChannel channel) {
        return InteractionResponses.messageAsEmbed("pong!");
    }

    private InteractionResponses helloWorld(Member executor, TextChannel channel) {
        String message = String.format("Hello, %s!", executor.getEffectiveName());
        return InteractionResponses.messageAsEmbed(message);
    }

    private InteractionResponses args(TextChannel channel, List<String> args) {
        String message = String.format("args: %s", String.join(", ", args));
        return InteractionResponses.messageAsEmbed(message);
    }
}
