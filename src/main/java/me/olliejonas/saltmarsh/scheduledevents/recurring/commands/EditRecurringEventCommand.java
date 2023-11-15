package me.olliejonas.saltmarsh.scheduledevents.recurring.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEvent;
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
import java.util.stream.Stream;

public class EditRecurringEventCommand extends Command {

    private final WizardEmbedManager wizardEmbedManager;

    private final RecurringEventManager manager;

    public EditRecurringEventCommand(WizardEmbedManager wizardEmbedManager, RecurringEventManager manager) {
        super(CommandPermissions.EVENTS, "edit-recurring-events");

        this.wizardEmbedManager = wizardEmbedManager;
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Edit how often an event recurs!");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Guild guild = executor.getGuild();
        List<ScheduledEvent> events = guild.getScheduledEvents().stream().filter(manager::isRecurring).toList();

        if (events.isEmpty()) return InteractionResponses.error("There aren't any recurring events! :(");

        return wizardEmbedManager.register(executor, channel, buildEmbed(guild, executor, events));
    }

    private WizardEmbed buildEmbed(Guild guild, Member executor, List<ScheduledEvent> events) {
        List<Button> buttons = Utils.fromEvents(events, null);
        String title = "Edit Recurring Event Wizard";

        return WizardEmbed.builder()
                .step(StepMenu.Button.builder("events", title,
                                "Which event would you like to edit?").buttons(buttons)
                        .build())
                .step(StepMenu.Button.builder("frequency", title, "How often would you like this event to repeat?")
                        .buttons(Stream.of("Daily", "Weekly", "Bi-Weekly", "Monthly")
                                .map(s -> Button.primary(s, s)).toList())
                        .build())

                .completionPage(WizardEmbed.GENERIC_COMPLETION_PAGE(title))

                .onCompletion(results -> {
                    String eventName = (String) results.get("events");
                    RecurringEvent.Frequency frequency = RecurringEvent.Frequency.from((String) results.get("frequency"));
                    ScheduledEvent event = guild.getScheduledEventsByName(eventName, false).get(0);

                    manager.update(RecurringEvent.of(event, executor, frequency), guild);

                    return InteractionResponses.messageAsEmbed("Successfully marked " + eventName + " as repeating!", true);
                })
                .build();
    }

}
