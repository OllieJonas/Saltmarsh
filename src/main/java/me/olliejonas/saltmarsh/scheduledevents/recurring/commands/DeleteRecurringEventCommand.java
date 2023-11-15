package me.olliejonas.saltmarsh.scheduledevents.recurring.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import me.olliejonas.saltmarsh.scheduledevents.recurring.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.Map;

public class DeleteRecurringEventCommand extends Command {

    private final WizardEmbedManager wizardEmbedManager;
    private final RecurringEventManager manager;

    public DeleteRecurringEventCommand(WizardEmbedManager wizardEmbedManager, RecurringEventManager manager) {
        super(CommandPermissions.EVENTS, "delete-recurring-event");

        this.wizardEmbedManager = wizardEmbedManager;
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Un-mark an event as recurring! (Doesn't delete the event)");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Guild guild = executor.getGuild();
        List<ScheduledEvent> events = guild.getScheduledEvents().stream().filter(manager::isRecurring).toList();

        if (events.isEmpty()) return InteractionResponses.error("There aren't any recurring events! :(");

        return wizardEmbedManager.register(executor, channel, buildEmbed(guild, events));
    }


    private WizardEmbed buildEmbed(Guild guild, List<ScheduledEvent> events) {
        List<Button> buttons = Utils.fromEvents(events, null);

        return WizardEmbed.builder()
                .step(StepMenu.Button.builder("event",
                                "Delete Recurring Event Wizard",
                                "Please select a recurring event you would like to remove " +
                                        "\n(Note: This doesn't delete the whole event, just stops it from recurring)")
                        .buttons(buttons)
                        .build())
                .onCompletion(results -> {
                    String name = (String) results.get("event");
                    String eventId = guild.getScheduledEventsByName(name, false).get(0).getId();
                    manager.remove(eventId);
                    return InteractionResponses.messageAsEmbed("Successfully removed " + name + " as a recurring event!");
                })
                .build();
    }
}
