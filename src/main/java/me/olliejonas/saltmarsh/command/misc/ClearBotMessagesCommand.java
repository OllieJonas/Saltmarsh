package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;

public class ClearBotMessagesCommand extends Command {

    static int RETRIEVE_PAST_COUNT = 50;

    public ClearBotMessagesCommand() {
        super(CommandPermissions.ADMIN, "clear-bot", "clear-bot-messages");
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Checks previous " + RETRIEVE_PAST_COUNT + " messages, and removes any sent by Saltmarsh.");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        channel.getHistory().retrievePast(RETRIEVE_PAST_COUNT)
                .queue(messages -> {
                    List<Message> saltmarsh = messages.stream()
                            .filter(message -> message.getAuthor().isBot()
                                    && message.getAuthor().getName().equals(Constants.APP_TITLE))
                            .toList();

                    RestAction<Void> action = null;

                    for (Message message : saltmarsh) {
                        if (action == null)
                            action = message.delete();
                        else action = action.and(message.delete());
                    }

                    if (action != null)
                        action.queue();
                });
        return InteractionResponses.empty();
    }
}
