package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class GetConfigurationCommand extends Command {

    private final boolean developerMode;

    private final ScheduledEventManager scheduledEventManager;

    private final RecurringEventManager recurringEventManager;


    public GetConfigurationCommand(boolean developerMode, ScheduledEventManager scheduledEventManager, RecurringEventManager recurringEventManager) {
        super(CommandPermissions.ADMIN, "get-config");

        this.developerMode = developerMode;
        this.scheduledEventManager = scheduledEventManager;
        this.recurringEventManager = recurringEventManager;
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Guild guild = executor.getGuild();
        EmbedBuilder builder = EmbedUtils.fromAsBuilder("Configuration", "Guild: " + executor.getGuild().getName());

        final String NOT_APPLICABLE = "N/A";

        String scheduledEventChannel = scheduledEventManager.getChannel(guild).map(Channel::getName).orElse(NOT_APPLICABLE);
        String scheduledEventRole = scheduledEventManager.getRole(guild).map(Role::getName).orElse(NOT_APPLICABLE);

        String recurringEventsDumpChannel = recurringEventManager.getChannel(guild).map(Channel::getName).orElse(NOT_APPLICABLE);

        builder.addField("Scheduled Events",
                "notification_channel: " + scheduledEventChannel + "\nnotification_role: " + scheduledEventRole,
                false);

        builder.addField("Recurring Events",
                "dump_channel: " + recurringEventsDumpChannel, false);
        builder.addField("Misc", "developer_mode: " + developerMode, false);

        return InteractionResponses.embed(builder.build());
    }
}
