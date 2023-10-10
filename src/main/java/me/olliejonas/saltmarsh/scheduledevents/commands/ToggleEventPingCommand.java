package me.olliejonas.saltmarsh.scheduledevents.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ToggleEventPingCommand extends Command {

    private final ScheduledEventManager manager;

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "text-channel", "The text channel you'd like these notifications to appear in!")
        );
    }

    public ToggleEventPingCommand(ScheduledEventManager manager) {
        super(CommandPermissions.ADMIN, "toggle-event-ping");
        this.manager = manager;
    }


    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        TextChannel targetChannel = channel;

        if (args.containsKey("text-channel")) {
            GuildChannelUnion gChannel = args.get("text-channel").getAsChannel();
            if (!(gChannel instanceof TextChannel tChannel)) return InteractionResponses.error("Please specify a text channel for this!");
            targetChannel = tChannel;
        }
        boolean removed = manager.toggleNotification(channel.getGuild(), targetChannel);

        return InteractionResponses.messageAsEmbed("Successfully " + (removed ? "removed " : "added ") +
                targetChannel.getName() + " to list of channels to receive event notifications!", true);
    }
}
