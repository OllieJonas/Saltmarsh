package me.olliejonas.saltmarsh.embed.input.types;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Function;

public interface InputCandidate<T> {

    enum Method {
        BUTTON,
        TEXT;
    }

    MessageCreateData compile();

    default Function<String, Integer> skip() {
        return __ -> 1;
    }

    String identifier();

    MessageEmbed embed();

    Class<T> clazz();
}
