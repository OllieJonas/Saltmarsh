package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

public class PollEmbedManager {

    private final WeakConcurrentHashMap<String, PollEmbed> embedMap;

    private final ButtonEmbedManager manager;

    public PollEmbedManager(ButtonEmbedManager manager) {
        this(manager, new WeakConcurrentHashMap<>());
    }

    public PollEmbedManager(ButtonEmbedManager manager, WeakConcurrentHashMap<String, PollEmbed> embedMap) {
        this.manager = manager;
        this.embedMap = embedMap;
    }

    public void send(Member sender, TextChannel channel, PollEmbed embed) {
        send(sender, channel, embed, false);
    }

    public void send(Member sender, TextChannel channel, PollEmbed embed, boolean notifyChannel) {
        if (notifyChannel)
            channel.sendMessage("@here " + sender.getEffectiveName() + " has sent a poll!").queue(__ -> doSend(channel, embed));
        else doSend(channel, embed);
    }

    private void doSend(TextChannel channel, PollEmbed embed) {
        manager.send(channel, embed.toEmbed(), message -> embedMap.put(message.getId(), embed));
    }

    public Optional<PollEmbed> get(String messageId) {
        return Optional.of(embedMap.get(messageId));
    }
}
