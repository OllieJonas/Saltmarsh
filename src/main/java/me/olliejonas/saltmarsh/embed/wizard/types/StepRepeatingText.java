package me.olliejonas.saltmarsh.embed.wizard.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.EntryContext;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jooq.lambda.function.Consumer3;
import org.jooq.lambda.tuple.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// options is purely internal
public record StepRepeatingText<T>(String identifier, MessageEmbed embed, Class<T> clazz, String nextText, AtomicInteger skipAmount,
                                   BiPredicateWithContext<T, StepCandidate<T>> valid, Consumer3<StepCandidate<T>, T, Method> onOptionSel, List<T> options)
        implements StepCandidate<T> {

    public static <T> StepRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new StepRepeatingText<>(identifier, embed, clazz, "Next",
                new AtomicInteger(), (__, ___) -> new Tuple2<>(true, ""), (__, ___, ____) -> {}, new ArrayList<>());
    }

    public static <T> StepRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz, BiPredicateWithContext<T, StepCandidate<T>> valid) {
        return new StepRepeatingText<>(identifier, embed, clazz, "Next", new AtomicInteger(),
                valid, (__, ___, ____) -> {}, new ArrayList<>());
    }

    public static <T> StepRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz,
                                              String nextText, BiPredicateWithContext<T, StepCandidate<T>> valid) {
        return new StepRepeatingText<>(identifier, EmbedUtils.colour()
                .setTitle(title)
                .setDescription(description)
                .build(), clazz, nextText, new AtomicInteger(),
                valid, (__, ___, ____) -> {}, new ArrayList<>());
    }

    public static <T> StepRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), clazz);
    }

    @Override
    public boolean requiresText() {
        return true;
    }

    @Override
    public MessageCreateData compile(boolean showExitButton) {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", String.format("""
                        Please type your answer into the same channel that this is sent in!
                        Press "%s" when you're done!
                        
                        **Current Input**
                        """ +
                        options.stream().map(Object::toString).collect(Collectors.joining(", ")), nextText), false)
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
          if (ctx.method() == Method.BUTTON && (ctx.component() != null && ((Button) ctx.component()).getLabel().equals(nextText())))
              setSkip(1);
          else options.add(ctx.result());
        });
    }

    @Override
    public int skip() {
        return skipAmount.get();
    }
}
