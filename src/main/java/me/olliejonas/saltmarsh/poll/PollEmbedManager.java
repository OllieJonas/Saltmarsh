package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PollEmbedManager {

    private final WeakConcurrentHashMap<String, PollEmbed> embedMap;

    private final ButtonEmbedManager manager;

    public PollEmbedManager(ButtonEmbedManager manager) {
        this(manager, new WeakConcurrentHashMap<>(TimeUnit.HOURS.toMillis(24)));
    }

    public PollEmbedManager(ButtonEmbedManager manager, WeakConcurrentHashMap<String, PollEmbed> embedMap) {
        this.manager = manager;
        this.embedMap = embedMap;
    }

    public void send(TextChannel channel, PollEmbed embed) {
        manager.send(channel, embed.toEmbed(), message -> embedMap.put(message.getId(), embed));
    }

    public Optional<PollEmbed> get(String messageId) {
        return Optional.of(embedMap.get(messageId));
    }
}
