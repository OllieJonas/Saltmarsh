package me.olliejonas.saltmarsh.command.meta.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import me.olliejonas.saltmarsh.embed.button.derivations.Itemizable;
import me.olliejonas.saltmarsh.embed.button.derivations.ItemizedEmbed;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    record Item(String command, Set<String> aliases, CommandInfo info) implements Itemizable, Comparable<Item> {

        public static Item from(Command command) {
            return new Item(command.getMetadata().primaryAlias(), command.getMetadata().aliases(), command.info());
        }

        @Override
        public String representation() {
            return "-" + command + (aliases.isEmpty() ? "" : " (" + aliases.stream()
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
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
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

        manager.register(embed.toPaginatedEmbed());
        return empty();
    }
}
