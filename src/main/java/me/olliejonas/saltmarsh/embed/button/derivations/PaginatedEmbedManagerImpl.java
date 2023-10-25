package me.olliejonas.saltmarsh.embed.button.derivations;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class PaginatedEmbedManagerImpl implements PaginatedEmbedManager {

    private final ButtonEmbedManager buttonEmbedManager;
    private final Map<String, PaginatedEmbed> paginatedEmbedMap;


    public PaginatedEmbedManagerImpl(ButtonEmbedManager buttonEmbedManager) {
        this.buttonEmbedManager = buttonEmbedManager;
        this.paginatedEmbedMap = new HashMap<>();
    }

    public InteractionResponses register(PaginatedEmbed paginatedEmbed,
                                         Function<PaginatedEmbed, Optional<ButtonEmbed>> initialEmbedFunction, Runnable onFailure) {
        ButtonEmbed compiled = initialEmbedFunction.apply(paginatedEmbed).orElse(null);

        if (compiled == null)
            onFailure.run();

        return buttonEmbedManager.register(compiled, message -> paginatedEmbedMap.put(message.getId(), paginatedEmbed));
    }

    public Optional<PaginatedEmbed> get(String messageId) {
        return Optional.ofNullable(paginatedEmbedMap.get(messageId));
    }

    public void remove(String messageId) {
        paginatedEmbedMap.remove(messageId);
    }
}
