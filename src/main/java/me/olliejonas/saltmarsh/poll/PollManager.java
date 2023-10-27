package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

public sealed interface PollManager permits PollManagerImpl {

    String SQL_POLL_SPLIT = "|";

    int SQL_OPTION_LENGTH = 1000;

    int QUESTION_MAX_LENGTH = 1000;

    static int MAX_COMBINED_OPTION_LENGTH(int noOptions) {
        return SQL_OPTION_LENGTH - noOptions;
    }

    default InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed) {
        return send(sender, channel, embed, false);
    }

    InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed, boolean notifyChannel);

    Optional<PollEmbed> get(String messageId);

    InteractionResponses vote(ButtonEmbed.ClickContext context);
}
