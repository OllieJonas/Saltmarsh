package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// options is purely internal
public record InputRepeatingText<T>(String identifier, MessageEmbed embed, Class<T> clazz, String exitText,
                                    Predicate<T> valid, AtomicInteger skipAmount, List<T> options)
        implements InputCandidate<T> {

    public static <T> InputRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new InputRepeatingText<>(identifier, embed, clazz, "Next", __ -> true, new AtomicInteger(), new ArrayList<>());
    }

    public static <T> InputRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz, Predicate<T> valid) {
        return new InputRepeatingText<>(identifier, embed, clazz, "Next", valid, new AtomicInteger(), new ArrayList<>());
    }

    public static <T> InputRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), clazz);
    }

    @Override
    public MessageCreateData compile() {
        return buildEmbed();
    }

    public MessageCreateData buildEmbed() {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", String.format("""
                        Please type your answer into the same channel that this is sent in!
                        Press "%s" when you're done!
                        """, exitText), false)
                .addField("", "", true)
                .addField("Current Input", options.stream().map(Object::toString).collect(Collectors.joining(", ")), false)
                .build();
        return new MessageCreateBuilder().setEmbeds(newEmbed).setActionRow(Button.success("_", exitText), EXIT_BUTTON).build();
    }

    @Override
    public BiConsumer<T, Method> onOptionSelection() {
        return (value, method) -> {
          if (method == Method.BUTTON)
              skipAmount.set(1);
          else options.add(value);
        };
    }

    @Override
    public int skip() {
        return skipAmount.get();
    }
}
