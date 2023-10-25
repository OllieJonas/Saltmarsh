package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

public sealed interface PollEmbedManager permits PollEmbedManagerImpl {
    default InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed) {
        return send(sender, channel, embed, false);
    }

    InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed, boolean notifyChannel);

    Optional<PollEmbed> get(String messageId);
}
