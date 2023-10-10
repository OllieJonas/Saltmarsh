package me.olliejonas.saltmarsh.embed.button.derivations;

import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
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
        System.out.println(channel.getName());
        send(embed, PaginatedEmbed::curr, () -> {
            System.out.println("failed!");
        });
    }

    public void send(TextChannel channel, PaginatedEmbed embed, Runnable onFailure) {
        send(embed, PaginatedEmbed::curr, onFailure);
    }

    public void send(PaginatedEmbed paginatedEmbed, Function<PaginatedEmbed, Optional<ButtonEmbed>> initialEmbedFunction, Runnable onFailure) {
        initialEmbedFunction.apply(paginatedEmbed).ifPresentOrElse(embed -> manager.send(embed, message -> paginatedEmbedMap.put(message.getId(), paginatedEmbed)), onFailure);
    }

    public Optional<PaginatedEmbed> get(String messageId) {
        return Optional.ofNullable(paginatedEmbedMap.get(messageId));
    }
}
