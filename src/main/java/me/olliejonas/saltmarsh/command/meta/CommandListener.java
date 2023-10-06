package me.olliejonas.saltmarsh.command.meta;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.function.Consumer2;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {

    private final CommandRegistry registry;

    private final CommandWatchdog watchdog;

    public CommandListener(CommandRegistry registry, CommandWatchdog watchdog, String prefix) {
        this(registry, watchdog);
    }

    public CommandListener(@NotNull CommandRegistry registry, @NotNull CommandWatchdog watchdog) {
        this.registry = registry;
        this.watchdog = watchdog;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Collection<? extends CommandData> commandData = registry.getCommandMap().entrySet().stream()
                .filter(e -> e.getValue().shouldRegisterAsSlashCommand())
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
    public void onSlashCommandInteraction(final @NotNull SlashCommandInteractionEvent event) {
        Consumer2<MessageEmbed, Boolean> onFailureReplyCons =
                (embed, ephemeral) -> event.replyEmbeds(embed).setEphemeral(ephemeral).queue();

        String root = "";
        Throwable exception = null;

        try {
            root = event.getName();
            Command command = registry.getOrThrow(root);
            Map<String, OptionMapping> args = event.getOptions().stream()
                    .collect(Collectors.toMap(OptionMapping::getName, map -> map));

            if (args.containsKey("subcommand")) {
                command = command.getSubCommands().getOrDefault(args.get("subcommand").getAsString(), command);
                args.remove("subcommand");
            }

            Member executor = event.getMember();
            TextChannel channel = event.getChannel().asTextChannel();
            CommandPermissions permissions = command.getPermissions();

            if (permissions != null && executor != null && !permissions.hasPermission(executor))
                throw CommandFailedException.noPermission(executor, command);

            InteractionResponses action = command.execute(executor, channel, args, root);

            // just to clarify, don't rely on this null check. it's bad. but there will be some lazy commands that only
            // you will use, so treat yourself a little, let yourself commit sin and return null on a command,
            // * you know you want to * ;)
            if (action == null)
                action = InteractionResponses.empty();

            action.queue(event, channel);

        } catch (CommandFailedException ex) {
            exception = ex;
            onFailureReplyCons.accept(EmbedUtils.error(ex.getMessage()), ex.isResponseEphemeral());
        } catch (Throwable t) {
            exception = t;
            onFailureReplyCons.accept(EmbedUtils.error(Constants.UNKNOWN_ERROR_PROMPT(root, t.getLocalizedMessage())), false);
            t.printStackTrace();
        }
    }
}
