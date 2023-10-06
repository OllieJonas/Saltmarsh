package me.olliejonas.saltmarsh.embed.input.types;

import lombok.Getter;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class InputButtonEmbed<T> implements InputTargetEmbed {

    private final String identifier;

    private final MessageEmbed embed;

    private final List<Button> buttons;
    private final Class<T> clazz;

    private AtomicReference<T> storedValue;

    public InputButtonEmbed(String identifier, MessageEmbed embed, Class<T> clazz, List<Button> buttons) {
        this.identifier = identifier;
        this.embed = embed;
        this.clazz = clazz;
        this.buttons = buttons;
    }

    public void compile(InputEmbedManager manager) {

    }

    public static Builder<String> builder(String identifier) {
        return new Builder<>(identifier, String.class);
    }

    public static <T> Builder<T> builder(String identifier, Class<T> clazz) {
        return new Builder<>(identifier, clazz);
    }

    public static <T> Builder<T> builder(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new Builder<>(identifier, embed, clazz);
    }

    public static class Builder<T> {
        private final String identifier;
        private final Class<T> clazz;

        private MessageEmbed embed;

        private List<Button> buttons;


        public Builder(String identifier, Class<T> clazz) {
            this(identifier, null, clazz, new ArrayList<>());
        }
        public Builder(String identifier, MessageEmbed embed, Class<T> clazz) {
            this(identifier, embed, clazz, new ArrayList<>());
        }

        public Builder(String identifier, MessageEmbed embed, Class<T> clazz, List<Button> buttons) {
            this.identifier = identifier;
            this.embed = embed;
            this.clazz = clazz;
            this.buttons = buttons;
        }

        public Builder<T> embed(MessageEmbed embed) {
            this.embed = embed;
            return this;
        }

        public Builder<T> option(String text) {
            return option(Button.primary(identifier, text));
        }

        public Builder<T> option(Emoji emoji) {
            if (clazz != String.class && clazz != Boolean.class)
                throw new IllegalArgumentException("can only use either String or Booleans with Emojis!");

            return option(Button.primary(identifier, emoji));
        }

        public Builder<T> option(Button button) {
            if (StringToTypeConverter.cantCast(button.getLabel(), clazz))
                throw new IllegalArgumentException("you need to be able to cast your button value!");

            buttons.add(button.withId(identifier));
            return this;
        }

        public Builder<T> options(List<Button> options) {
            if (options.stream().anyMatch(opt -> StringToTypeConverter.cantCast(opt.getLabel(), clazz)))
                throw new IllegalArgumentException("you need to be able to cast your buttons!");

            this.buttons = options;
            return this;
        }

        public InputButtonEmbed<T> build() {
            return new InputButtonEmbed<>(identifier, embed, clazz, buttons);
        }
    }
}
