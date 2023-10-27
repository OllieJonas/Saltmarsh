package me.olliejonas.saltmarsh.music.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.olliejonas.saltmarsh.util.TimeUtils;

public record TrackInfo(String author, String title, String identifier, String uri, boolean isStream, long durationMillis) {
    public static TrackInfo from(AudioTrack track) {
        return from(track.getInfo());
    }

    public static TrackInfo from(AudioTrackInfo info) {
        return new TrackInfo(info.author, info.title, info.identifier, info.uri, info.isStream, info.length);
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
