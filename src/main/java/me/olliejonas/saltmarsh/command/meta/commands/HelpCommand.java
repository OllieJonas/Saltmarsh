package me.olliejonas.saltmarsh.command.meta.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandRegistry;
import me.olliejonas.saltmarsh.util.embed.Itemizable;
import me.olliejonas.saltmarsh.util.embed.ItemizedEmbed;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    record Item(String command, Set<String> aliases, CommandInfo info) implements Itemizable, Comparable<Item> {

        public static Item from(Command command) {
            return new Item(command.getPrimaryAlias(), command.getAliases(), command.info());
        }

        @Override
        public String representation() {
            return "-" + command + (aliases.size() == 0 ? "" : " (" + aliases.stream()
                    .map(s -> "-" + s).sorted().collect(Collectors.joining(", ")) + ")") +
                    ItemizedEmbed.AS_FIELD_SPLIT_STR + info.shortDesc();
        }

        @Override
        public int compareTo(@NotNull Item other) {
            return command().compareTo(other.command());
        }
    }

    private final PaginatedEmbedManager manager;

    private final CommandRegistry registry;

    public HelpCommand(PaginatedEmbedManager manager, CommandRegistry registry) {
        super("help");
        this.manager = manager;
        this.registry = registry;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Displays a list of all commands available to you!");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        List<Item> items = registry.getCommandMap().values().stream()
                .filter(command -> command.hasPermission(executor))
                .filter(Command::isRoot)
                .distinct()
                .map(Item::from)
                .sorted()
                .toList();

        ItemizedEmbed<Item> embed = ItemizedEmbed.<Item>builder()
                .title("Help")
                .author(executor.getEffectiveName())
                .items(items)
                .pageCount(true)
                .asFields(true)
                .build();

        manager.send(channel, embed.compile(manager));
        return empty();
    }
}
