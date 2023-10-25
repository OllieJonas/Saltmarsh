package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

public interface InputEmbedManager {

    record Context(String sender, String message, InputEmbed embed) {}

    void destroy(TextChannel channel);

    Context getContext(TextChannel channel);

    default InputEmbed getEmbed(TextChannel channel) {
        return getContext(channel).embed();
    }

    default String getMessageId(TextChannel channel) {
        return getContext(channel).message();
    }

    boolean isNotInteracting(Member sender, TextChannel channel);

    default InteractionResponses register(TextChannel channel, InputEmbed embed) {
        return register(null, channel, embed);
    }

    // if sender is null, then anyone is able to interact with the InputEmbed.
    // otherwise, only the sender can.
    InteractionResponses register(@Nullable Member sender, TextChannel channel,
                                  InputEmbed embed);
}
