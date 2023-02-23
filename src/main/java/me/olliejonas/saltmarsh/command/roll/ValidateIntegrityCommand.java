package me.olliejonas.saltmarsh.command.roll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ValidateIntegrityCommand extends Command {

    private static final int INTEGRITY_DICE_SIZE = 100_000;

    private static final int INTEGRITY_DICE_CEIL = 6;

    private final Random random;

    public ValidateIntegrityCommand(Random random) {
        super(CommandPermissions.ADMIN, "validateintegrity", "validate", "integrity", "montecarlo");
        this.random = random;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.of("Roll 100,000 6-sided dice and get some statistics!", "Perform a monte carlo-esque style of random analysis.");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {

        int expectedCountPer = INTEGRITY_DICE_SIZE / INTEGRITY_DICE_CEIL;
        List<Integer> ints = random.ints(INTEGRITY_DICE_SIZE, 1,
                INTEGRITY_DICE_CEIL + 1).boxed().toList();
        double expectedRoll = IntStream.range(1, INTEGRITY_DICE_CEIL + 1).average().orElse(0);
        double expectedSum = expectedRoll * INTEGRITY_DICE_SIZE;

        double actualSum = ints.stream().mapToInt(i -> i).sum();
        double actualAverage = ints.stream().mapToInt(i -> i).average().orElse(0);

        return InteractionResponses.embed(asEmbed(random.getClass().getPackageName(),
                ints, expectedCountPer,
                actualSum, expectedSum,
                actualAverage, expectedRoll
        ));
    }

    private MessageEmbed asEmbed(String method, List<Integer> counts, int expectedCountPer, double sum, double expectedSum, double average, double expectedAverage) {
        EmbedBuilder builder = EmbedUtils.standard();
        builder.setTitle("Roll Integrity (ADMIN ONLY)");
        builder.setDescription(INTEGRITY_DICE_SIZE + " random numbers with max " + INTEGRITY_DICE_CEIL);
        builder.addField("Method", method, false);

        builder.addField("Counts (vs Expected)", String.format("%s (vs %d per)", getCountStr(counts), expectedCountPer), false);
        builder.addField("Average (vs Expected)", String.format("%f (vs %f)", average, expectedAverage), false);
        builder.addField("Sum (vs Expected)", String.format("%f (vs %f)", sum, expectedSum), false);

        return builder.build();
    }

    private String getCountStr(List<Integer> counts) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= INTEGRITY_DICE_CEIL; i++) {
            builder.append(i);
            builder.append(": ");
            builder.append(Collections.frequency(counts, i));

            if (i != INTEGRITY_DICE_CEIL)
                builder.append(", ");
        }

        return builder.toString();
    }
}
