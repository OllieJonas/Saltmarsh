package me.olliejonas.saltmarsh.command.meta;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple3;

import java.util.*;

@Getter
public abstract class Command {

    private final CommandPermissions permissions;
    private final String primaryAlias;

    @Setter
    private Command parent;

    private final Map<String, Command> subCommands;

    private final Set<String> aliases;

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
        this.permissions = permissions;
        this.primaryAlias = primaryAlias;
        this.aliases = aliases;
        this.subCommands = subCommands;
    }

    public abstract CommandInfo commandInfo();

    public abstract InteractionResponses execute(Member executor, TextChannel channel,
                                                 List<String> args, String aliasUsed) throws CommandFailedException;

    public void addSubCommands() {}

    public boolean registerAsSlashCommand() {
        return true;
    }

    protected void addSubCommand(Command command) {
        command.setParent(this);
        subCommands.put(command.getPrimaryAlias(), command);
        command.getAliases().forEach(als -> subCommands.put(als, command));
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

    private String helpStr() {
        return "this is some help!";
    }

    public Tuple3<Command, List<String>, Integer> traverse(List<String> tokens) {
        Command curr = this;
        int consumed = 1;
        String root;

        do {
            tokens = tokens.subList(1, tokens.size());

            if (curr.getSubCommands().size() == 0
                    || tokens.size() == 0
                    || !curr.getSubCommands().containsKey(tokens.get(0)))
                break;

            root = tokens.get(0);
            curr = curr.getSubCommands().get(root);
            consumed++;
        } while (true);

        return new Tuple3<>(curr, tokens, consumed);
    }
}
