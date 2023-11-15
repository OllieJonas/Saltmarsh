package me.olliejonas.saltmarsh.scheduledevents.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ToggleEventPingRolesCommand extends Command {

    private final ScheduledEventManager manager;

    public ToggleEventPingRolesCommand(ScheduledEventManager manager) {
        super(CommandPermissions.ADMIN, "toggle-event-ping-roles");
        this.manager = manager;
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.ROLE, "role", "The role you want to receive notifications! (You can only designate one role at a time)", true)
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Role role = args.get("role").getAsRole();
        boolean removed = manager.toggleRole(executor.getGuild(), role);

        return InteractionResponses.messageAsEmbed("Successfully " + (removed ? "removed " : "designated ") + role.getName() + " as the role to receive notifications!", true);
    }
}
