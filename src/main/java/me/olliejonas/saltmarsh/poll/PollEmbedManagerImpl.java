package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PollEmbedManagerImpl implements PollEmbedManager {

    private final Map<String, PollEmbed> embedMap;

    private final ButtonEmbedManager manager;

    public PollEmbedManagerImpl(ButtonEmbedManager manager) {
        this(manager, new HashMap<>());
    }

    public PollEmbedManagerImpl(ButtonEmbedManager manager, Map<String, PollEmbed> embedMap) {
        this.manager = manager;
        this.embedMap = embedMap;
    }

    public InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed, boolean notifyChannel) {
        if (notifyChannel)
            channel.sendMessage("@here " + sender.getEffectiveName() + " has sent a poll!").complete();

        return manager.register(embed.toEmbed(), message -> embedMap.put(message.getId(), embed));
    }

    public Optional<PollEmbed> get(String messageId) {
        return Optional.of(embedMap.get(messageId));
    }
}
