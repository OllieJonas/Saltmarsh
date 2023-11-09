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

        return info.author + " - " + info.title.replace(info.author, "").strip() + " (" + minutes(length) + ":" + seconds(length) + ")";
    }

    private String seconds(long millis) {
        int seconds = doSeconds(millis);
        return (seconds < 10 ? "0" : "") + seconds;
    }

    private String minutes(long millis) {
        return String.valueOf(doMinutes(millis));
    }

    private int doSeconds(long millis) {
        return (int) ((millis / 1000) % 60);
    }

    private int doMinutes(long millis) {
        return (int) ((millis / 1000) / 60);
    }
}
