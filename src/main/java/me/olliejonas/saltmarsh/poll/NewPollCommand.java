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

import java.util.List;

public class NewPollCommand extends Command {

    private PaginatedEmbedManager manager;

    public NewPollCommand(PaginatedEmbedManager manager) {
        super(CommandPermissions.ADMIN, "newpoll");
        this.manager = manager;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        PaginatedEmbed embed = PaginatedEmbed.standard()
                .embed(EmbedUtils.fromAsBuilder("Poll", "Hello! What would you like your question to be?"))
                .embed(EmbedUtils.fromAsBuilder("Poll", "Is this a Yes-No poll?"))

                .build();

        manager.send(channel, embed);
        return null;
    }
}
