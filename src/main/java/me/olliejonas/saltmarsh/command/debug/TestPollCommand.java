package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.poll.PollEmbed;
import me.olliejonas.saltmarsh.poll.PollManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestPollCommand extends Command {

    private final PollManager manager;

    public TestPollCommand(PollManager manager) {
        super(CommandPermissions.ADMIN, "poll");
        this.manager = manager;
    }
    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        PollEmbed singular = PollEmbed.builder()
                .author(executor.getEffectiveName())
                .question("Do you like cats? (SINGULAR)")
                .option("yes")
                .option("no")
                .option("why")
                .singularVotes()
                .textRepresented()
                .anonymous(false)
                .build();

        PollEmbed nonSingular = PollEmbed.builder()
                .author(executor.getEffectiveName())
                .question("Do you like cats? (NON-SINGULAR)")
                .option("yes")
                .option("no")
                .option("why")
                .textRepresented()
                .anonymous(false)
                .build();

        manager.send(executor, channel, singular).queue(null, channel);
        manager.send(executor, channel, nonSingular).queue(null, channel);

        return InteractionResponses.messageAsEmbed("Sent command!", false);
    }
}
