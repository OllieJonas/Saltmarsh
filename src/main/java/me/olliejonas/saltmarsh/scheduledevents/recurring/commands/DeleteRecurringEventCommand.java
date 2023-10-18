package me.olliejonas.saltmarsh.scheduledevents.recurring.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.input.InputEmbed;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.Map;

public class DeleteRecurringEventCommand extends Command {

    static int BUTTON_MAX_LENGTH = 80;

    private final InputEmbedManager inputEmbedManager;
    private final RecurringEventManager manager;

    public DeleteRecurringEventCommand(InputEmbedManager inputEmbedManager, RecurringEventManager manager) {
        super(CommandPermissions.EVENTS, "delete-recurring-event");

        this.inputEmbedManager = inputEmbedManager;
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Mark an event as not recurring! (Doesn't delete event)");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Guild guild = executor.getGuild();
        List<ScheduledEvent> events = guild.getScheduledEvents().stream().filter(manager::isRecurring).toList();

        if (events.isEmpty()) return InteractionResponses.error("There aren't any recurring events! :(");

        return inputEmbedManager.createEmbed(executor, channel, buildEmbed(guild, events)).v1();
    }


    private InputEmbed buildEmbed(Guild guild, List<ScheduledEvent> events) {
        List<Button> buttons = events.stream()
                .map(event -> new Tuple2<>(event.getId(), event.getName()))
                .map(tuple -> tuple.map2(str -> str.length() >= BUTTON_MAX_LENGTH ? str.substring(0, BUTTON_MAX_LENGTH - 3) + "..." : str))
                .map(tuple -> Button.primary(tuple.v1(), tuple.v2())).toList();

        return InputEmbed.builder()
                .step(InputMenu.Button.builder("event")
                        .buttons(buttons)
                        .build())
                .onCompletion(results -> {
                    String name = (String) results.get("event");
                    String eventId = guild.getScheduledEventsByName(name, false).get(0).getId();
                    manager.removeEvent(eventId);
                    return InteractionResponses.messageAsEmbed("Successfully removed " + name + " as a recurring event!");
                })
                .build();
    }
}
