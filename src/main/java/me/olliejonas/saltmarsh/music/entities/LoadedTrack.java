package me.olliejonas.saltmarsh.music.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.olliejonas.saltmarsh.music.Queueable;
import me.olliejonas.saltmarsh.util.TimeUtils;
import me.olliejonas.saltmarsh.embed.button.derivations.Itemizable;
import net.dv8tion.jda.api.entities.User;

import java.util.function.Supplier;

public class LoadedTrack implements Queueable, Itemizable {

    private final Supplier<AudioTrack> trackSupplier;

    private final Info info;

    private boolean isLoaded;

    private AudioTrack cached;

    private final User owner;

    public LoadedTrack(Supplier<AudioTrack> trackSupplier, Info info, User owner) {
        this.trackSupplier = trackSupplier;
        this.info = info;
        this.owner = owner;
        this.isLoaded = false;
    }

    @Override
    public void onQueued() {
        cached = trackSupplier.get();
        this.isLoaded = true;
    }

    public Supplier<AudioTrack> trackSupplier() {
        return trackSupplier;
    }

    public AudioTrack track() {
        return cached;
    }

    public User owner() {
        return owner;
    }

    @Override
    public String id() {
        return info.identifier();
    }

    public Info info() {
        return info;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public String representation() {
        return info.representation();
    }

    public record Info(String author, String title, String identifier, String uri, boolean isStream, long durationMillis) {
        public static Info from(AudioTrack track) {
            return from(track.getInfo());
        }

        public static Info from(AudioTrackInfo info) {
            return new Info(info.author, info.title, info.identifier, info.uri, info.isStream, info.length);
        }

        public String searchQuery() {
            return author + " - " + title;
        }

        public String representation() {
            return searchQuery() + " (" + duration() + ")";
        }

        public String duration() {
            return TimeUtils.secondsToString(durationMillis / 1000);
        }
    }
}
