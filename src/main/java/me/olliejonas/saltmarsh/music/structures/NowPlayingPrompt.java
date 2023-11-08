package me.olliejonas.saltmarsh.music.structures;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.atomic.AtomicReference;

public class NowPlayingPrompt {

    private static final String TITLE = "Now playing";

    private static final MessageEmbed EMPTY_PROMPT = EmbedUtils.colour(TITLE, "Nothing is currently playing! :(");

    private final AtomicReference<Message> message;

    public NowPlayingPrompt() {
        this.message = new AtomicReference<>();
    }

    public void sendInitial(TextChannel channel, AudioTrack track) {
        channel.sendMessageEmbeds(embed(track)).queue(this.message::set);
    }

    public void onNextItem(AudioTrack track) {
        message.get().editMessage(MessageEditData.fromEmbeds(embed(track))).queue();
    }

    private MessageEmbed embed(AudioTrack track) {
        return track == null ? EMPTY_PROMPT : EmbedUtils.colour(TITLE, representation(track));
    }

    private String representation(AudioTrack track) {
        return new TrackRepresentation(track).toString();
    }
}
