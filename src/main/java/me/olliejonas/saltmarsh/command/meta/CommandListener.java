package me.olliejonas.saltmarsh.command.meta;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {

    private final CommandRegistry registry;

    private final CommandWatchdog watchdog;

    private final Collection<String> prefixes;

    public CommandListener(CommandRegistry registry, CommandWatchdog watchdog, String prefix) {
        this(registry, watchdog, Collections.singleton(prefix));
    }

    public CommandListener(@NotNull CommandRegistry registry, @NotNull CommandWatchdog watchdog,
                           @NotNull Collection<String> prefixes) {
        this.registry = registry;
        this.watchdog = watchdog;
        this.prefixes = prefixes;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Collection<? extends CommandData> commandData = registry.getCommandMap().entrySet().stream()
                .filter(e -> e.getValue().registerAsSlashCommand())
                .filter(e -> e.getValue().info() != null)
                .map(e -> {
                    Command command = e.getValue();
                    SlashCommandData data = Commands.slash(e.getKey(), e.getValue().info().shortDesc());
                    data.addOptions(command.args());
                    return data;
                })
                .collect(Collectors.toSet());

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannelType().isMessage() || !event.getChannelType().isGuild()) return; // wasn't a message in text channel, we don't care

        executeFrom(null, event.getMessage(), event.getMember(), event.getChannel().asTextChannel(), event.getMessage().getContentRaw());
    }

    @Override
    public void onSlashCommandInteraction(final @NotNull SlashCommandInteractionEvent event) {
        executeFrom(event, null, event.getMember(), event.getChannel().asTextChannel(), event.getCommandString());
    }

    private void executeFrom(@Nullable SlashCommandInteractionEvent event,
                             @Nullable Message message, Member executor, TextChannel channel, String messageStr) {
        Consumer2<MessageEmbed, Boolean> onFailureReplyCons =
                event == null ? (embed, __) -> channel.sendMessageEmbeds(embed).queue() :
                        (embed, ephemeral) -> event.replyEmbeds(embed).setEphemeral(ephemeral).queue();

        String root = "";
        Throwable exception = null;

        if (!isValidCommandSyntax(messageStr) && event == null) return;

        try {
            root = getRootFrom(event, messageStr);
            Command command = registry.getOrThrow(root);  // no null checks required, throws CommandFailedException
            Tuple3<Command, List<String>, Integer> toExecute = command.traverse(List.of(messageStr.split(" ")));

            CommandPermissions permissions = toExecute.v1().getPermissions();

            if (permissions != null && executor != null && !permissions.hasPermission(executor))
                throw CommandFailedException.noPermission(executor, command);

            List<String> args = event == null ? toExecute.v2() : collectArgs(event);

            InteractionResponses action = toExecute.v1().execute(executor, channel, args, root);

            // just to clarify, don't rely on this null check. it's bad. but there will be some lazy commands that only
            // you will use, so treat yourself a little, let yourself commit sin and return null on a command,
            // * you know you want to * ;)
            if (action != null)
                action.queue(event, channel);

        } catch (CommandFailedException ex) {
            exception = ex;
            onFailureReplyCons.accept(EmbedUtils.error(ex.getMessage()), ex.isResponseEphemeral());

        } catch (Throwable t) {
            exception = t;
            onFailureReplyCons.accept(EmbedUtils.error(Constants.UNKNOWN_ERROR_PROMPT(messageStr, t.getLocalizedMessage())), false);
            t.printStackTrace();
        }

        // watchdog
        CommandWatchdog.Report report = new CommandWatchdog.Report(exception, executor, channel, root, messageStr);
        watchdog.report(report);
    }

    private List<String> collectArgs(SlashCommandInteractionEvent event) {
        return event.getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.toList());
    }

    private boolean isValidCommandSyntax(@NotNull String message) {
        return message.length() > 0 && prefixes.stream().anyMatch(message::startsWith);
    }

    private String getRootFrom(SlashCommandInteractionEvent event, @NotNull String messageRaw) {
        if (event != null) {
            return event.getName();
        } else {
            AtomicReference<String> root = new AtomicReference<>(messageRaw.split(" ")[0]);
            prefixes.forEach(pfx -> root.updateAndGet(curr -> curr.replaceFirst(pfx, "")));
            return root.get();
        }
    }
}
