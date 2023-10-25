package me.olliejonas.saltmarsh.scheduledevents.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;

public class GetEventPingStatusCommand extends Command {


    private final ScheduledEventManager manager;

    private static final Set<String> OPTIONS = Set.of(
            "channels",
            "roles"
    );

    public GetEventPingStatusCommand(ScheduledEventManager manager) {
        super(CommandPermissions.ADMIN, "get-event-ping-status");
        this.manager = manager;
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "type", "The type of thing you'd like to " +
                        "get the status of ! (Options are: " + String.join(", ", OPTIONS + ")"))
        );
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        String option = args.get("type").getAsString();

        return switch (option.toLowerCase(Locale.ROOT)) {
            case "channels" -> getChannels(executor.getGuild());
            case "roles" -> getRoles(executor.getGuild());
            default -> InteractionResponses.error("Please specify one of " +
                    String.join(", ", OPTIONS + "!"));
        };
    }

    private InteractionResponses getChannels(Guild guild) {
        return InteractionResponses.message(manager.getChannel(guild)
                .map(channel -> channel.getName() + " is currently receiving notifications!")
                .orElse("There aren't any channels currently receiving notifications!"));
    }

    private InteractionResponses getRoles(Guild guild) {
        return InteractionResponses.messageAsEmbed(manager.getRole(guild)
                .map(role -> "The current role to receive notifications is \"" + role.getName() + "\"!")
                .orElse("No role is currently assigned!"));
    }
}
