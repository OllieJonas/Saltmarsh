package me.olliejonas.saltmarsh.command.meta;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;

@Getter
public abstract class Command {

    protected static final OptionData SUBCOMMAND_ARG = new OptionData(OptionType.STRING, "subcommand", "Subcommands!", false);

    protected static final OptionData FORCED_SUBCOMMAND_ARG = new OptionData(OptionType.STRING, "subcommand", "Subcommands!", true);

    public record Metadata(CommandPermissions permissions, String primaryAlias, Set<String> aliases) {}

    private final Metadata metadata;

    @Setter
    private Command parent;

    private final Map<String, Command> subCommands;

    public Command(String alias) {
        this(CommandPermissions.ALL, alias, Collections.emptySet());
    }

    public Command(String primaryAlias, String... aliases) {
        this(CommandPermissions.ALL, primaryAlias, Set.of(aliases));
    }

    public Command(CommandPermissions permissions, String primaryAlias) {
        this(permissions, primaryAlias, Collections.emptySet());
    }

    public Command(CommandPermissions permissions, String primaryAlias, String... aliases) {
        this(permissions, primaryAlias, Set.of(aliases));
    }

    public Command(CommandPermissions permissions, String primaryAlias, Set<String> aliases) {
        this(permissions, primaryAlias, aliases, new HashMap<>());
    }

    public Command(CommandPermissions permissions, String primaryAlias, Set<String> aliases, Map<String, Command> subCommands) {
        this.metadata = new Metadata(permissions, primaryAlias, aliases);
        this.subCommands = subCommands;
    }

    public CommandInfo info() {
        return CommandInfo.empty();
    }

    public abstract InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel,
                                                 Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException;

    public InteractionResponses execute(Member executor, TextChannel channel,
                        Map<String, OptionMapping> args, String aliasUsed) {
        return execute(null, executor, channel, args, aliasUsed);
    }

    public void addSubCommands() {}

    public boolean shouldRegisterAsSlashCommand() {
        return true;
    }

    protected void addSubCommand(Command command) {
        command.setParent(this);
        subCommands.put(command.getMetadata().primaryAlias(), command);
        command.getMetadata().aliases().forEach(als -> subCommands.put(als, command));
    }

    public Collection<OptionData> args() {
        return Collections.emptyList();
    }


    protected void sendMessage(TextChannel channel, String message) throws CommandFailedException {
        channel.sendMessage(message).queue((__) -> {},
                (__) -> { throw CommandFailedException.jdaInternal(
                                String.format("Unable to send message to channel %s", channel.getName()));});
    }

    protected void sendEmbedMessage(TextChannel channel, MessageEmbed embed) throws CommandFailedException {
        channel.sendMessageEmbeds(embed).queue((__) -> {},
                (__) -> { throw CommandFailedException.jdaInternal(
                        String.format("Unable to send message to channel %s", channel.getName()));});
    }

    protected InteractionResponses help() {
        return InteractionResponses.messageAsEmbed(helpStr());
    }

    protected InteractionResponses empty() {
        return InteractionResponses.empty();
    }

    private String helpStr() {
        return "this is some help!";
    }

    public Tuple2<Command, Integer> traverse(List<String> tokens) {
        Command curr = this;
        int consumed = 1;
        String root;

        do {
            tokens = tokens.subList(1, tokens.size());

            if (curr.getSubCommands().isEmpty() || tokens.isEmpty() || !curr.getSubCommands().containsKey(tokens.get(0)))
                break;

            root = tokens.get(0);
            curr = curr.getSubCommands().get(root);
            consumed++;
        } while (true);

        return new Tuple2<>(curr, consumed);
    }

    public boolean hasPermission(Member executor) {
        return metadata.permissions() != null && metadata.permissions().hasPermission(executor);
    }

    public boolean isRoot() {
        return parent == null;
    }
}
