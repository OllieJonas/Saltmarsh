package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.button.derivations.Itemizable;
import me.olliejonas.saltmarsh.embed.button.derivations.ItemizedEmbed;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;
import java.util.stream.Collectors;

public class TestItemizedEmbedCommand extends Command {

    private final PaginatedEmbedManager manager;

    public TestItemizedEmbedCommand(PaginatedEmbedManager manager) {
        super(CommandPermissions.ADMIN, "iembed");
        this.manager = manager;
    }
    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        ItemizedEmbed<Itemizable.Strings> embed = ItemizedEmbed.<Itemizable.Strings>builder()
                .title("This is a title!")
                .author("This is an author!")
                .item(new Itemizable.Strings("testing!"))
                .item(new Itemizable.Strings("123!"))
                .item(new Itemizable.Strings("yan sucks lmao"))
                .itemsPerPage(1)
                .displayIndex()
                .pageCount()
                .build();

        System.out.println(embed.items().stream().map(Itemizable.Strings::representation).collect(Collectors.toList()));

        return manager.register(embed.toPaginatedEmbed());
    }
}
