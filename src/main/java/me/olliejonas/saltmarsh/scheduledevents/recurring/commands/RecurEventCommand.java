package me.olliejonas.saltmarsh.scheduledevents.recurring.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.input.InputEmbed;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEvent;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import me.olliejonas.saltmarsh.scheduledevents.recurring.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RecurEventCommand extends Command {

    public static int BUTTON_MAX_LENGTH = 80;

    private final InputEmbedManager inputEmbedManager;

    private final RecurringEventManager manager;


    public RecurEventCommand(InputEmbedManager inputEmbedManager,
                             RecurringEventManager manager) {
        super(CommandPermissions.EVENTS, "recur-event");
        this.inputEmbedManager = inputEmbedManager;
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Mark an event as recurring!");
    }

    @SuppressWarnings("ConstantConditions") // this is here for ctx.component(); not null because it's in a button menu
    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Guild guild = executor.getGuild();
        List<ScheduledEvent> events = executor.getGuild().getScheduledEvents();

        List<Button> eventButtons = Utils.fromEvents(events, manager);

        if (eventButtons.isEmpty())
            return InteractionResponses.error("There aren't any valid events that can be marked for recurring! :(");

        String title = "Recurring Event Wizard";

        InputEmbed embed = InputEmbed.builder()
                .step(InputMenu.Button.builder("events", title,
                        "Which event would you like to make recurring?").buttons(eventButtons)
                        .build())
                .step(InputMenu.Button.builder("frequency", title, "How often would you like this event to repeat?")
                        .buttons(Stream.of("Daily", "Weekly", "Bi-Weekly", "Monthly")
                                .map(s -> Button.primary(s, s)).toList())
                        .build())

                .completionPage(InputEmbed.GENERIC_COMPLETION_PAGE(title))

                .onCompletion(results -> {
                    String eventName = (String) results.get("events");
                    RecurringEvent.Frequency frequency = RecurringEvent.Frequency.from((String) results.get("frequency"));
                    ScheduledEvent event = guild.getScheduledEventsByName(eventName, false).get(0); // TODO: Add which buttons / select options they pressed into results!
                    manager.register(RecurringEvent.of(event, executor, frequency), guild);
                    return InteractionResponses.messageAsEmbed("Successfully marked " + eventName + " as repeating!", true);
                })
                .build();

        return inputEmbedManager.register(executor, channel, embed);
    }
}
