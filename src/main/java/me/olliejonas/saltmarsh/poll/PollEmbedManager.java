package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
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

    public InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed, boolean notifyChannel) {
        if (notifyChannel)
            channel.sendMessage("@here " + sender.getEffectiveName() + " has sent a poll!").complete();

        return doSend(embed);
    }

    private InteractionResponses doSend(PollEmbed embed) {
        return manager.send(embed.toEmbed(), message -> embedMap.put(message.getId(), embed));
    }

    public Optional<PollEmbed> get(String messageId) {
        return Optional.of(embedMap.get(messageId));
    }
}
