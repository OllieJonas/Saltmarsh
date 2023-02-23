package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbed;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class FrequentlyAskedQuestionsCommand extends Command {

    private final PaginatedEmbedManager manager;

    public FrequentlyAskedQuestionsCommand(PaginatedEmbedManager manager) {
        super("frequentyaskedquestions",  "faq");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        PaginatedEmbed embed = Constants.FAQ_EMBED.get();
        embed.compile(manager);
        manager.send(channel, embed);
        return InteractionResponses.empty();
    }
}
