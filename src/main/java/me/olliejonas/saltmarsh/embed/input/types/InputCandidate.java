package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.input.EntryContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface InputCandidate<T> {


    Button EXIT_BUTTON = Button.danger("exit", "Exit");

    enum Method {
        SELECT,

        BUTTON,
        TEXT;
    }

    MessageCreateData compile(boolean showExitButton);

    default int skip() {
        return 1;
    }

    // this is supported in: ButtonBuilder (for Confirmation Screen), InputRepeatingText (obvious reasons)
    default void setSkip(int skip) {
        throw new IllegalArgumentException("This step doesn't support altering the skip amount!");
    }

    String identifier();

    MessageEmbed embed();

    Class<T> clazz();

    // self, value, method
    Consumer<EntryContext<T>> onOption();

     Predicate<T> valid();
}
