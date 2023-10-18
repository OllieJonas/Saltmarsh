package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.EntryContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jooq.lambda.function.Consumer3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// options is purely internal
public record InputRepeatingText<T>(String identifier, MessageEmbed embed, Class<T> clazz, String nextText, AtomicInteger skipAmount,
                                    Predicate<T> valid, Consumer3<InputCandidate<T>, T, Method> onOptionSel, List<T> options)
        implements InputCandidate<T> {

    public static <T> InputRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new InputRepeatingText<>(identifier, embed, clazz, "Next",
                new AtomicInteger(), __ -> true, (__, ___, ____) -> {}, new ArrayList<>());
    }

    public static <T> InputRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz, Predicate<T> valid) {
        return new InputRepeatingText<>(identifier, embed, clazz, "Next", new AtomicInteger(),
                valid, (__, ___, ____) -> {}, new ArrayList<>());
    }

    public static <T> InputRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), clazz);
    }

    @Override
    public MessageCreateData compile(boolean showExitButton) {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", String.format("""
                        Please type your answer into the same channel that this is sent in!
                        Press "%s" when you're done!
                        """, nextText), false)
                .addField("", "", true)
                .addField("Current Input", options.stream().map(Object::toString).collect(Collectors.joining(", ")), false)
                .build();

        List<ActionRow> rows = new ArrayList<>();
        rows.add(ActionRow.of(Button.success("_", nextText)));

        if (showExitButton)
            rows.add(ActionRow.of(EXIT_BUTTON));

        return new MessageCreateBuilder().setEmbeds(newEmbed).setComponents(rows).build();
    }

    @Override
    public void setSkip(int skip) {
        skipAmount.set(skip);
    }

    @Override
    public Consumer<EntryContext<T>> onOption() {
        return ((ctx) -> {
          if (ctx.method() == Method.BUTTON)
              setSkip(1);
          else options.add(ctx.result());
        });
    }

    @Override
    public int skip() {
        return skipAmount.get();
    }
}
