package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.poll.PollEmbed;
import me.olliejonas.saltmarsh.poll.PollEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestPollCommand extends Command {

    private final PollEmbedManager manager;

    public TestPollCommand(PollEmbedManager manager) {
        super(CommandPermissions.ADMIN, "poll");
        this.manager = manager;
    }
    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        PollEmbed embed = PollEmbed.builder(manager)
                .author(executor.getEffectiveName())
                .question("Do you like cats?")
                .option("yes")
                .option("no")
                .option("why")
                .build();

        manager.send(channel, embed);

        return InteractionResponses.empty();
    }
}
