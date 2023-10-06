package me.olliejonas.saltmarsh.embed;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PaginatedEmbedManager {

    private final ButtonEmbedManager manager;
    private final Map<String, PaginatedEmbed> paginatedEmbedMap;


    public PaginatedEmbedManager(ButtonEmbedManager manager) {
        this.manager = manager;
        this.paginatedEmbedMap = new HashMap<>();
    }

    public void send(TextChannel channel, PaginatedEmbed embed) {
        send(channel, embed, PaginatedEmbed::curr, () -> {});
    }

    public void send(TextChannel channel, PaginatedEmbed embed, Runnable onFailure) {
        send(channel, embed, PaginatedEmbed::curr, onFailure);
    }

    public void send(TextChannel channel, PaginatedEmbed paginatedEmbed, Function<PaginatedEmbed, Optional<ButtonEmbed>> initialEmbedFunction, Runnable onFailure) {
        initialEmbedFunction.apply(paginatedEmbed).ifPresentOrElse(embed -> manager.send(channel, embed, message -> paginatedEmbedMap.put(message.getId(), paginatedEmbed)), onFailure);
    }

    public Optional<PaginatedEmbed> get(String messageId) {
        return Optional.ofNullable(paginatedEmbedMap.get(messageId));
    }
}
