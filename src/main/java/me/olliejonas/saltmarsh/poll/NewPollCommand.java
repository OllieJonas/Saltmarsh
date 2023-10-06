package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.PaginatedEmbed;
import me.olliejonas.saltmarsh.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class NewPollCommand extends Command {

    private final PaginatedEmbedManager manager;

    public NewPollCommand(PaginatedEmbedManager manager) {
        super(CommandPermissions.ADMIN, "newpoll");
        this.manager = manager;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        PaginatedEmbed embed = PaginatedEmbed.standard()
                .embed(EmbedUtils.fromAsBuilder("Poll", "Hello! What would you like your question to be?"))
                .embed(EmbedUtils.fromAsBuilder("Poll", "Is this a Yes-No poll? (The options are either yes or no)"))
                .embed(EmbedUtils.fromAsBuilder("Poll",
                        "What options would you like to include?" +
                                " (Press Done when you are finished with your options)"))
                .embed(EmbedUtils.fromAsBuilder("Poll", "Would you like this poll to be anonymous?"))
                .embed(EmbedUtils.fromAsBuilder("Poll", "Would you like this poll to be singular or multiple voting? (people can vote for only one option or multiple options)"))

                .build();

        embed.compile(manager);

        manager.send(channel, embed);

        return InteractionResponses.messageAsEmbed("here we are");
    }
}
