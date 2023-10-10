package me.olliejonas.saltmarsh.scheduledevents.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;
import java.util.stream.Collectors;

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
        Set<TextChannel> channels = manager.getChannelsFor(guild);
        return InteractionResponses.message("There are " + channels.size() + " channels currently receiving notifications! " +
                "These are: " + channels.stream().map(Channel::getAsMention).collect(Collectors.joining(", ")));
    }

    private InteractionResponses getRoles(Guild guild) {
        Role role = manager.getRole(guild);
        return InteractionResponses.messageAsEmbed(role == null ? "No role is currently assigned!" :
                "The current role to receive notifications is \"" + role.getName() + "\"!");
    }
}
