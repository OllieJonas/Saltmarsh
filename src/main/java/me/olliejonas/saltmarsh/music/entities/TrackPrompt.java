package me.olliejonas.saltmarsh.music.entities;

import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.atomic.AtomicReference;

public class TrackPrompt implements AudioQueue.Listener<LoadedTrack> {
    private static final MessageEmbed EMPTY_PROMPT = EmbedUtils.standard().setTitle("Now playing").build();
    private final AtomicReference<Message> message;

    private boolean alreadySent;

    public TrackPrompt() {
        this.message = new AtomicReference<>();
        this.alreadySent = false;
    }

    public void init(TextChannel channel) {
        if (!alreadySent) {
            channel.sendMessageEmbeds(EMPTY_PROMPT).queue(msg -> {
                message.set(msg);
                alreadySent = true;
            });
        }
    }

    @Override
    public void onNextItem(LoadedTrack next) {
        message.get().editMessage(MessageEditData.fromEmbeds(embed(next))).queue();
    }

    @Override
    public void onQueueEmpty() {
        System.out.println("queue is empty!");
        message.get().editMessage(MessageEditData.fromEmbeds(EmbedUtils.from("Nothing! :("))).queue();
    }

    private MessageEmbed embed(LoadedTrack track) {
        EmbedBuilder builder = EmbedUtils.standard();
        builder.setTitle("Now playing");
        builder.setDescription(track.info().searchQuery());
        return builder.build();
    }
}
