package me.olliejonas.saltmarsh.embed.input;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.embed.ButtonEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
@Setter
public class InputButtonEmbed<T> implements InputTargetEmbed {

    private final ButtonEmbed embed;

    private final Class<T> clazz;

    private T storedValue;


    protected InputButtonEmbed(ButtonEmbed embed, Class<T> clazz) {
        this.embed = embed;
        this.clazz = clazz;
    }

    public static class Builder<T> {

        private MessageEmbed embed;

        private Class<T> clazz;

        public Builder(MessageEmbed embed, Class<T> clazz) {
            this.embed = embed;
            this.clazz = clazz;
        }

        public Builder<T> option(String text, T output) {
            return this;
        }

        public InputButtonEmbed<T> build() {
            return null;
        }
    }

}
