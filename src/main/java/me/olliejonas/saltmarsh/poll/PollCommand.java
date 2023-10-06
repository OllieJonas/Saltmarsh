package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PollCommand extends Command {

    private static final Pattern POLL_PATTERN = Pattern.compile("^(.*)\\\\? (([^|]+)(?: \\\\| ([^|]+)){0,9})$");

    private final PollEmbedManager manager;

    public PollCommand(PollEmbedManager manager) {
        super(CommandPermissions.ADMIN, "poll");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        String argsStr = String.join(" ", args);
        Matcher matcher = POLL_PATTERN.matcher(argsStr);

        System.out.println(argsStr);
        if (!matcher.find())
            throw CommandFailedException.badArgs(executor, this, "question ? option 1 | option 2 | option 3 ");

        String question = matcher.group(1);

        List<PollOption> pollOptions = Arrays.stream(matcher.group(2).split(" \\|"))
                .map(PollOption::new)
                .toList();

        if (pollOptions.isEmpty()) throw CommandFailedException.badArgs(executor, this, "option 1 | option 2 | option ...");
        if (pollOptions.size() > 10) throw CommandFailedException.other("You can't have more than 10 options!", "no more than 10 options");

        PollEmbed embed = PollEmbed.builder(manager)
                .author(executor.getEffectiveName())
                .question(question + "?")
                .singularVotes()
                .options(pollOptions)
                .build();

        manager.send(channel, embed);
        return InteractionResponses.empty();
    }

    @Override
    public Collection<OptionData> args() {
        return List.of();
    }


}
