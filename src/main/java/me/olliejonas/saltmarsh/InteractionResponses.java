package me.olliejonas.saltmarsh;

import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    record Embed(MessageEmbed embed, boolean ephemeral, MessageEmbed... embeds) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            if (event == null) {
                channel.sendMessageEmbeds(embed).queue();
            }
            else {
                event.replyEmbeds(embed, embeds).setEphemeral(ephemeral).queue();
            }
        }
    }
}
