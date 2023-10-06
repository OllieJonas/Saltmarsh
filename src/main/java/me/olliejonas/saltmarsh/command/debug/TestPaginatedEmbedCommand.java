package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbed;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class TestPaginatedEmbedCommand extends Command {

    private final PaginatedEmbedManager manager;

    public TestPaginatedEmbedCommand(PaginatedEmbedManager manager) {
        super(CommandPermissions.ADMIN, "paginated-embed", "pembed", "p-embed");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {

        PaginatedEmbed.Builder builder = PaginatedEmbed.standard();
        builder.textPage("This is some text")
                .textPage("Ain't this cool!")
                .textPage("Like suuuuuper cool!")
                .textPage("hey yan you suck xoxo")
                .textPage("like reeeeal bad xoxoxoxo");

        PaginatedEmbed embed = builder.build();
        embed.compile(manager);
        manager.send(channel, embed);

        return InteractionResponses.empty();
    }
}
