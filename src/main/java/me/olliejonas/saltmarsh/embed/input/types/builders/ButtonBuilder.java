package me.olliejonas.saltmarsh.embed.input.types.builders;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ButtonBuilder<T> {

    private final String identifier;

    private final Class<T> clazz;

    private List<Button> buttons;

    private MessageEmbed embed;

    private Predicate<T> valid;

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
        this.valid = __ -> true;
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

    public ButtonBuilder<T> valid(Predicate<T> predicate) {
        this.valid = predicate;
        return this;
    }

    public ButtonBuilder<T> valid(Collection<Predicate<T>> predicates) {
        return valid(predicates.stream().reduce(Predicate::and).orElse(__ -> true));
    }

    public InputMenu.Button<T> build() {
        return new InputMenu.Button<>(identifier, clazz, embed, buttons, valid);
    }
}
