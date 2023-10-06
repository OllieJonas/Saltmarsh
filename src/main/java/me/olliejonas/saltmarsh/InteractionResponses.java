package me.olliejonas.saltmarsh;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public interface InteractionResponses {

    static InteractionResponses empty() {
        return new InteractionResponses.Empty();
    }

    static InteractionResponses messageAsEmbed(String message) {
        return messageAsEmbed(message, false);
    }
    static InteractionResponses messageAsEmbed(String message, boolean ephemeral) {
        return new InteractionResponses.Embed(EmbedUtils.from(message), ephemeral);
    }

    static InteractionResponses embed(MessageEmbed embed, MessageEmbed... embeds) {
        return new InteractionResponses.Embed(embed, false, embeds);
    }

    static InteractionResponses message(String message) {
        return new InteractionResponses.Message(message, false);
    }

    default void queue(IReplyCallback event, TextChannel channel) {
        queue(event, channel, null);
    }

    void queue(IReplyCallback event, TextChannel channel, net.dv8tion.jda.api.entities.Message original);

    record Message(String message, boolean ephemeral) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel, net.dv8tion.jda.api.entities.Message original) {
            if (event == null)
                channel.sendMessage(message).queue();
            else
                event.reply(message).setEphemeral(ephemeral).queue();
        }
    }

    record Empty() implements InteractionResponses {

        @Override
        public void queue(IReplyCallback event, TextChannel channel, net.dv8tion.jda.api.entities.Message original) {
            if (event == null) return;

            event.reply("Success!").setEphemeral(true).queue(message -> event.getHook().deleteOriginal().queue());
        }
    }

    record Reaction(Emoji emoji) implements InteractionResponses {

        @Override
        public void queue(IReplyCallback event, TextChannel channel, net.dv8tion.jda.api.entities.Message original) {
            if (original != null) // null when slash command is used
                original.addReaction(emoji).queue();

            if (event == null) // null when text command is used
                return;

            empty();
        }
    }

    record Embed(MessageEmbed embed, boolean ephemeral, MessageEmbed... embeds) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel, net.dv8tion.jda.api.entities.Message original) {
            if (event == null) {
                channel.sendMessageEmbeds(embed).queue();
            }
            else {
                event.replyEmbeds(embed, embeds).setEphemeral(ephemeral).queue();
            }
        }
    }
}
