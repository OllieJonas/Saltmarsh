package me.olliejonas.saltmarsh;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    static InteractionResponses deferEmbed(Supplier<MessageEmbed> embed) {
        return deferEmbed(false, embed);
    }

    static InteractionResponses deferEmbed(boolean ephemeral, Supplier<MessageEmbed> embed) {
        return new DeferEmbed(() -> Collections.singleton(embed.get()), ephemeral);
    }

    static InteractionResponses embed(MessageEmbed embed, MessageEmbed... embeds) {
        return new InteractionResponses.Embed(embed, false, embeds);
    }

    static InteractionResponses message(String message) {
        return new InteractionResponses.Message(message, false);
    }

    static InteractionResponses error(String message) {
        return error(message, true);
    }

    static InteractionResponses error(Throwable t) {
        return error(t.getMessage(), true);
    }

    static InteractionResponses error(String message, boolean ephemeral) {
        return new InteractionResponses.Embed(EmbedUtils.error(message), ephemeral);
    }

    static InteractionResponses embedWithAttachment(MessageEmbed embed, FileUpload file) {
        return embedWithAttachments(embed, Collections.singleton(file));
    }

    static InteractionResponses embedWithAttachments(MessageEmbed embed, Collection<FileUpload> files) {
        return new EmbedWithAttachments(embed, files);
    }

    static InteractionResponses titleDescription(String title, String description) {
        return embed(EmbedUtils.from(title, description));
    }

    void queue(@Nullable IReplyCallback event, TextChannel channel);

    record Message(String message, boolean ephemeral) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            if (event == null)
                channel.sendMessage(message).queue();
            else
                event.reply(message).setEphemeral(ephemeral).queue();
        }
    }

    record EmbedWithAttachments(MessageEmbed embed, Collection<FileUpload> files) implements InteractionResponses {
        @Override
        public void queue(IReplyCallback event, TextChannel channel) {
            if (event == null) return;

            event.replyEmbeds(embed).addFiles(files).queue();
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
            if (event == null)
                channel.sendMessage(data).queue(onSuccess);
            else
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
                channel.sendMessageEmbeds(embed).queue(msg -> {
                    if (ephemeral) msg.delete().queue();
                });
            }
        }
    }

    record DeferEmbed(Supplier<Collection<? extends MessageEmbed>> embeds, boolean ephemeral) implements InteractionResponses {

        @Override
        public void queue(@Nullable IReplyCallback event, TextChannel channel) {
            if (event != null) {
                event.deferReply().setEphemeral(ephemeral).queue();
                event.getHook().sendMessageEmbeds(embeds.get()).setEphemeral(ephemeral).queue();
            }
        }
    }
}
