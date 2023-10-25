package me.olliejonas.saltmarsh.embed.button;

import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;
import java.util.function.Consumer;

public sealed interface ButtonEmbedManager permits ButtonEmbedManagerImpl {

    boolean exists(String id);

    Optional<ButtonEmbed> get(String id);

    default InteractionResponses register(ButtonEmbed embed) {
        return register(embed, __ -> {});
    }

    InteractionResponses register(ButtonEmbed embed, Consumer<Message> onSuccess);

    void remove(String id);
}
