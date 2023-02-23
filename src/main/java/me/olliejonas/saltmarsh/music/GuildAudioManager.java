package me.olliejonas.saltmarsh.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import me.olliejonas.saltmarsh.music.entities.AudioQueue;
import me.olliejonas.saltmarsh.music.entities.Track;
import me.olliejonas.saltmarsh.music.entities.TrackPrompt;
import me.olliejonas.saltmarsh.music.exceptions.QueueException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class GuildAudioManager {

    private final Guild guild;

    private final AudioPlayer player;

    private final AudioPlayerSendHandler handler;

    private final GuildAudioAdapter adapter;

    private final AudioQueue<Track> queue;

    private final TrackPrompt trackPrompt;

    private final AtomicBoolean currentlyPlaying;
    private String joinedChannelId;


    public GuildAudioManager(Guild guild, AudioPlayer player) {
        this.guild = guild;
        this.player = player;

        this.currentlyPlaying = new AtomicBoolean(false);

        this.queue = new AudioQueue<>();

        this.trackPrompt = new TrackPrompt();
        queue.attachListener(trackPrompt);

        this.handler = new AudioPlayerSendHandler(player);

        this.adapter = new GuildAudioAdapter(this);
        player.addListener(adapter);
    }

    public void init(VoiceChannel voiceChannel, TextChannel textChannel) {
        this.joinedChannelId = voiceChannel.getId();
        trackPrompt.init(textChannel);
    }

    public void pause() {
        player.setPaused(true);
    }

    public void stop() {
        player.stopTrack();
    }

    public void skip(int amount) {
        if (queue.isEmpty())
            throw new QueueException(QueueException.Reason.EMPTY_QUEUE);

        Optional<Track> curr = queue.next();
        int tmp = 0;

        while (++tmp < amount && curr.isPresent()) {
            curr = queue.next();
        }

        curr.ifPresent(track -> player.startTrack(track.track(), false));
    }

    public boolean isSingleton() {
        return queue.size() == 1;
    }

    public void queue(AudioTrack track, User owner) {
        Track queuedTrack = new Track(track, owner);

        queue.add(queuedTrack);

        if (!currentlyPlaying.get()) {
            next();
        }
    }

    public boolean repeating() {
        return queue.toggleRepeating();
    }

    public void resume() {
        Track curr = queue.curr().orElseThrow(() -> new QueueException(QueueException.Reason.EMPTY_QUEUE));
        player.playTrack(curr.track());
    }

    public void next() {
        currentlyPlaying.set(false);
        queue.next().ifPresentOrElse(track -> {
            queue.getListeners().forEach(listener -> listener.onNextItem(track));
            player.startTrack(track.track(), false);
            currentlyPlaying.set(true);
            System.out.println("now playing " + track.track().getInfo().title + " by " + track.track().getInfo().author);
        }, () -> {
        });
    }
}
