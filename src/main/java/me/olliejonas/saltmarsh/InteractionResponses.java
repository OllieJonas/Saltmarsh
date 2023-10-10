package me.olliejonas.saltmarsh;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Consumer;

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

    static InteractionResponses createData(MessageCreateData data, boolean ephemeral, Consumer<net.dv8tion.jda.api.entities.Message> onSuccess) {
        return new InteractionResponses.CreateData(data, ephemeral, onSuccess);
    }

    static InteractionResponses embed(MessageEmbed embed, MessageEmbed... embeds) {
        return new InteractionResponses.Embed(embed, false, embeds);
    }

    static InteractionResponses message(String message) {
        return new InteractionResponses.Message(message, false);
    }

    static InteractionResponses error(String message) {
        return new InteractionResponses.Embed(EmbedUtils.error(message), true);
    }

    void queue(IReplyCallback event, TextChannel channel);

    record Message(String message, boolean ephemeral) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            if (event == null)
                channel.sendMessage(message).queue();
            else
                event.reply(message).setEphemeral(ephemeral).queue();
        }
    }

    record Empty() implements InteractionResponses {

        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            if (event == null) return;

            event.reply("Success!").setEphemeral(true).queue(message -> event.getHook().deleteOriginal().queue());
        }
    }

    record CreateData(MessageCreateData data, boolean ephemeral, Consumer<net.dv8tion.jda.api.entities.Message> onSuccess) implements InteractionResponses {

        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            event.reply(data).queue(hook -> hook.retrieveOriginal().queue(onSuccess));
        }
    }

    record Embed(MessageEmbed embed, boolean ephemeral, MessageEmbed... embeds) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            if (event != null) {
                event.replyEmbeds(embed, embeds).setEphemeral(ephemeral).queue();
            }
            else {
//                channel.sendMessageEmbeds(embed).queue(msg -> {
//                    if (ephemeral) msg.delete().queue();
//                });
            }
        }
    }
}
