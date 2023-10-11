package me.olliejonas.saltmarsh.embed.input.types;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public interface InputCandidate<T> {

    Button EXIT_BUTTON = Button.danger("exit", "Exit");

    enum Method {
        SELECT,

        BUTTON,
        TEXT;
    }

    MessageCreateData compile();

    default int skip() {
        return 1;
    }

    String identifier();

    MessageEmbed embed();

    Class<T> clazz();

    default BiConsumer<T, Method> onOptionSelection() {
        return (__, ___) -> {};
    }

     Predicate<T> valid();
}
