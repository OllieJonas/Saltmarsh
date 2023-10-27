package me.olliejonas.saltmarsh.embed.wizard.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.EntryContext;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Consumer;

public record StepText<T>(String identifier, MessageEmbed embed, Class<T> clazz,
                          Consumer<EntryContext<T>> onOption, BiPredicateWithContext<T, StepCandidate<T>> valid) implements StepCandidate<T> {

    public static <T> StepText<T> of(String identifier, String title, String description, Class<T> clazz,
                                     BiPredicateWithContext<T, StepCandidate<T>> valid) {
        return new StepText<>(identifier, EmbedUtils.colour()
                .setTitle(title)
                .setDescription(description)
                .build(), clazz, (__) -> {}, valid);
    }

    public static <T> StepText<T> of(String identifier, String title, String description, Class<T> clazz,
                                     Consumer<EntryContext<T>> onOption) {
        return new StepText<>(identifier, EmbedUtils.colour()
                .setTitle(title)
                .setDescription(description)
                .build(), clazz, onOption, (__, ___) -> new Tuple2<>(true, ""));
    }

    public static <T> StepText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, title, description, clazz, __ -> new Tuple2<>(true, ""));
    }

    public static <T> StepText<T> ofPredicate(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, title, description, clazz, (__, ___) -> new Tuple2<>(true, ""));
    }

    public static <T> StepText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new StepText<>(identifier, embed, clazz, (__) -> {}, (__, ___) -> new Tuple2<>(true, ""));
    }

    @Override
    public boolean requiresText() {
        return true;
    }

    public MessageCreateData compile(boolean showExitButton) {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", "Please type your answer into the same channel that this is sent in!", true)
                .build();

        MessageCreateBuilder builder = new MessageCreateBuilder().setEmbeds(newEmbed);
        if (showExitButton)
            builder.addActionRow(EXIT_BUTTON);

        return builder.build();
    }
}
