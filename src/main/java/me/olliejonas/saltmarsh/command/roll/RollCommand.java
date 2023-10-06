package me.olliejonas.saltmarsh.command.roll;

import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RollCommand extends Command {

    private static final Pattern DICE_REGEX = Pattern.compile("^(\\d+)(d)(\\d+)$");

    private static final String DICE_DESC = "\"<no dice>d<dice ceiling>\"";

    private final Random random;

    public RollCommand() {
        super("roll", "rolldice", "r");
        this.random = new Random();
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of(DICE_DESC, "Roll dice!",
                "Rolls a dice, using the same format you would as in Dungeons & Dragons.");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed)
            throws CommandFailedException {
        if (args.size() != 1)
            throw CommandFailedException.badArgs(executor, this, DICE_DESC);

        String diceInput = args.get(0);
        Matcher matcher = DICE_REGEX.matcher(diceInput);

        if (!matcher.matches())
            throw CommandFailedException.badArgs(executor, this, DICE_DESC);

        System.out.println(matcher.groupCount());
        // regex does int checks for us
        int noDice = Integer.parseInt(matcher.group(1));
        int diceCeil = Integer.parseInt(matcher.group(3));

        List<Integer> ints = random.ints(noDice, 1, diceCeil + 1).boxed().toList();
        double average = ints.stream().mapToInt(i -> i).average().orElse(0.0D);
        int sum = ints.stream().mapToInt(i -> i).sum();

        return InteractionResponses.embed(asEmbed(executor, noDice, diceCeil, ints, average, sum));
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "roll", DICE_DESC));
    }

    @Override
    public void addSubCommands() {
        addSubCommand(new ValidateIntegrityCommand(random));
    }

    private MessageEmbed asEmbed(Member executor, int noDice, int diceCeil, List<Integer> results, double average, int sum) {
        EmbedBuilder builder = EmbedUtils.standard();

        builder.setTitle(String.format("Dice Roll (%s)", executor.getEffectiveName()));
        builder.setDescription(String.format("No Dice: %d, Max Dice: %d (%s)", noDice, diceCeil, noDice + "d" + diceCeil));

        builder.addField("Rolls", results.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")), false);

        builder.addField("Sum", String.valueOf(sum), false);

        builder.addField("Average", String.valueOf(average), false);

        return builder.build();
    }
}
