package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PollCommand extends Command {

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
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "question", "the question you want to ask!", true),
                new OptionData(OptionType.STRING, "options", "the possible options! (separated by either ' | ' or ' : '", true),
                new OptionData(OptionType.BOOLEAN, "anonymous", "whether the voting should be anonymous! (defaults to true)"),
                new OptionData(OptionType.BOOLEAN, "singular", "whether users should be allowed to vote for multiple options or just one! (defaults to true)"),
                new OptionData(OptionType.NUMBER, "expiration-time", "the amount of time until it expires (defaults to 3)"),
                new OptionData(OptionType.STRING, "expiration-unit", "either minutes (m), hours (h) or days (d) (defaults to days)")
        );
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {

        String question = args.get("question").getAsString();
        String options = args.get("options").getAsString();
        boolean anonymous = true;
        boolean singular = true;

        long expirationTime = PollEmbed.DEFAULT_EXPIRATION_TIME;
        TimeUnit expirationUnit = PollEmbed.DEFAULT_EXPIRATION_UNITS;

        if (args.containsKey("anonymous"))
            anonymous = args.get("anonymous").getAsBoolean();

        if (args.containsKey("singular"))
            singular = args.get("singular").getAsBoolean();

        if (args.containsKey("expiration-time"))
            expirationTime = args.get("expirationTime").getAsLong();

        if (args.containsKey("expiration-unit")) {
            expirationUnit = strToTimeUnit(executor, args.get("expirationUnit").getAsString());
        }


        List<PollOption> pollOptions = Arrays.stream(options.split(" \\| "))
                .map(PollOption::new)
                .toList();

        if (pollOptions.isEmpty()) throw CommandFailedException.badArgs(executor, this, "option 1 | option 2 | option ...");
        if (pollOptions.size() > 10) throw CommandFailedException.other("You can't have more than 10 options!", "no more than 10 options");

        PollEmbed embed = PollEmbed.builder(manager)
                .author(executor.getEffectiveName())
                .question(question + "?")
                .anonymous(anonymous)
                .singularVotes(singular)
                .options(pollOptions)
                .build();


        manager.send(channel, embed);
        return InteractionResponses.messageAsEmbed("Successfully created poll!", true);
    }

    private TimeUnit strToTimeUnit(Member executor, String str) {
        return switch (str) {
            case "m", "minute", "minutes" -> TimeUnit.MINUTES;
            case "h", "hour", "hours" -> TimeUnit.HOURS;
            case "d", "day", "days" -> TimeUnit.DAYS;
            default -> throw CommandFailedException.badArgs(executor, this, "time unit is wrong!");
        };
    }
}
