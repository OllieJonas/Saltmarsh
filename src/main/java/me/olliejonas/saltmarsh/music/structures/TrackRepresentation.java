package me.olliejonas.saltmarsh.music.structures;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.olliejonas.saltmarsh.embed.button.derivations.Itemizable;

public record TrackRepresentation(AudioTrack track) implements Itemizable {

    @Override
    public String representation() {
        return toString();
    }

    @Override
    public String toString() {
        AudioTrackInfo info = track.getInfo();
        long length = info.length;

        return info.author + " - " + info.title + " (" + minutes(length) + ":" + seconds(length) + ")";
    }

    private int seconds(long millis) {
        return (int) ((millis / 1000) % 60);
    }

    private int minutes(long millis) {
        return (int) ((millis / 1000) / 60);
    }
}
