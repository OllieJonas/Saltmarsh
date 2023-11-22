package me.olliejonas.saltmarsh.embed.wizard.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.EntryContext;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
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
@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class StepRepeatingText<T> implements StepCandidate<T> {

    public static <T> StepRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new StepRepeatingText<>(identifier, embed, clazz,
                new AtomicInteger(1), (__, ___) -> new Tuple2<>(true, ""), (__, ___, ____) -> {});
    }

    public static <T> StepRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz, BiPredicateWithContext<T, StepCandidate<T>> valid) {
        return new StepRepeatingText<>(identifier, embed, clazz, new AtomicInteger(1),
                valid, (__, ___, ____) -> {});
    }

    public static <T> StepRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz,
                                              BiPredicateWithContext<T, StepCandidate<T>> valid) {
        return new StepRepeatingText<>(identifier, EmbedUtils.colour()
                .setTitle(title)
                .setDescription(description)
                .build(), clazz, new AtomicInteger(1),
                valid, (__, ___, ____) -> {});
    }

    public static <T> StepRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, EmbedUtils.colour(title, description), clazz);
    }

    public static <T> StepRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz, BiPredicateWithContext<T, StepCandidate<T>> valid, int skipAmount) {
        return new StepRepeatingText<>(identifier,
                EmbedUtils.colour(title, description),
                clazz,
                new AtomicInteger(skipAmount),
                (__, ___) -> new Tuple2<>(true, ""),
                (__, ___, ____) -> {});
    }

    private final String identifier;

    private final MessageEmbed embed;

    private final Class<T> clazz;

    private final AtomicInteger skipOnCompletion;

    private final BiPredicateWithContext<T, StepCandidate<T>> valid;

    private final Consumer3<StepCandidate<T>, T, Method> onOptionSel;


    private final AtomicInteger skipAmount = new AtomicInteger();

    private final List<T> options = new ArrayList<>();

    private final String nextText = "Next";

    private final String removePreviousItemText = "Remove Previous Item";

    private final List<Button> extraButtons = List.of(Button.success("_", nextText), Button.secondary("__", removePreviousItemText));

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
                        options.stream().map(this::optionToString).collect(Collectors.joining(", ")), nextText), false)
                .build();

        List<ActionRow> rows = new ArrayList<>();
        rows.add(ActionRow.of(extraButtons));

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
            if (ctx.method() == Method.BUTTON && ctx.component() != null) {

                String label = ((Button) ctx.component()).getLabel();
                if (label.equals(nextText)) {
                    setSkip(skipOnCompletion.get());

                } else if (label.equals(removePreviousItemText)) {
                    if (options.isEmpty())
                        return;

                    options.remove(options.size() - 1);
                } else {
                    options.add(ctx.result());
                }
            }

            if (ctx.method() != Method.BUTTON)
                options.add(ctx.result());

        });
    }

    @Override
    public int skip() {
        return skipAmount.get();
    }

    private String optionToString(T t) {
        if (t instanceof Member member) {
            return member.getAsMention();
        }

        else return t.toString();
    }
}
