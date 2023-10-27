package me.olliejonas.saltmarsh.embed.wizard.types.builders;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.EntryContext;
import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jooq.lambda.tuple.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ButtonBuilder<T> {

    private final String identifier;

    private final Class<T> clazz;

    private List<Button> buttons;

    private MessageEmbed embed;

    private BiPredicateWithContext<T, StepCandidate<T>> valid;

    private Consumer<EntryContext<T>> onOption;

    public ButtonBuilder(String identifier, Class<T> clazz) {
        this(identifier, null, clazz, new ArrayList<>());
    }

    public ButtonBuilder(String identifier, MessageEmbed embed, Class<T> clazz) {
        this(identifier, embed, clazz, new ArrayList<>());
    }

    public ButtonBuilder(String identifier, MessageEmbed embed, Class<T> clazz,
                         List<Button> buttons) {
        this.identifier = identifier;
        this.embed = embed;
        this.clazz = clazz;
        this.buttons = buttons;
        this.onOption = __ -> {};
        this.valid = (__, ___) -> new Tuple2<>(true, "ignored");
    }

    public ButtonBuilder<T> embed(String title, String description) {
        return embed(EmbedUtils.colour().setTitle(title).setDescription(description).build());
    }

    public ButtonBuilder<T> embed(MessageEmbed embed) {
        this.embed = embed;
        return this;
    }

    public ButtonBuilder<T> button(String text) {
        return button(Button.primary(String.valueOf(buttons.size()), text));
    }

    public ButtonBuilder<T> button(Emoji emoji) {
        if (clazz != String.class && clazz != Boolean.class)
            throw new IllegalArgumentException("can only use either String or Booleans with Emojis!");

        return button(Button.primary(String.valueOf(buttons.size()), emoji));
    }

    public ButtonBuilder<T> button(Button button) {
        buttons.add(button.withId(String.valueOf(buttons.size())));
        return this;
    }

    public ButtonBuilder<T> buttons(List<Button> options) {
        this.buttons = options;
        return this;
    }

    public ButtonBuilder<T> buttons(Button... buttons) {
        return buttons(List.of(buttons));
    }

    public ButtonBuilder<T> valid(BiPredicateWithContext<T, StepCandidate<T>> predicate) {
        this.valid = predicate;
        return this;
    }

    public ButtonBuilder<T> onOption(Consumer<EntryContext<T>> onOption) {
        this.onOption = onOption;
        return this;
    }

    public ButtonBuilder<T> valid(Collection<BiPredicateWithContext<T, StepCandidate<T>>> predicates) {
        return valid(predicates.stream().reduce(BiPredicateWithContext::and).orElse((__, ___) -> new Tuple2<>(true, "")));
    }

    public StepMenu.Button<T> build() {
        return StepMenu.Button.of(identifier, clazz, embed, buttons, onOption, valid);
    }
}
