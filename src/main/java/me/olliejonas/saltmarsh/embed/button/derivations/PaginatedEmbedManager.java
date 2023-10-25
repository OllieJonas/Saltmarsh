package me.olliejonas.saltmarsh.embed.button.derivations;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;

import java.util.Optional;
import java.util.function.Function;

public sealed interface PaginatedEmbedManager permits PaginatedEmbedManagerImpl {

    default InteractionResponses register(PaginatedEmbed embed) {
        return register(embed, PaginatedEmbed::curr, () -> {
            System.out.println("failed!");
        });
    }
    default InteractionResponses register(PaginatedEmbed embed, Runnable onFailure) {
        return register(embed, PaginatedEmbed::curr, onFailure);
    }
    InteractionResponses register(PaginatedEmbed paginatedEmbed,
                                  Function<PaginatedEmbed, Optional<ButtonEmbed>> initialEmbedFunction,
                                  Runnable onFailure);

    Optional<PaginatedEmbed> get(String messageId);

    void remove(String messageId);
}
