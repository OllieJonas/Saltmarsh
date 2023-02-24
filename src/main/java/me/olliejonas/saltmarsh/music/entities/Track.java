package me.olliejonas.saltmarsh.music.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.olliejonas.saltmarsh.music.Queueable;
import me.olliejonas.saltmarsh.util.TimeUtils;
import me.olliejonas.saltmarsh.util.embed.Itemizable;
import net.dv8tion.jda.api.entities.User;

public record Track(AudioTrack track, User owner) implements Queueable, Itemizable {
    @Override
    public String id() {
        return track.getIdentifier();
    }

    public AudioTrackInfo data() {
        return track.getInfo();
    }

    @Override
    public String representation() {
        return data().author + " - " + data().title + " (" + TimeUtils.secondsToString(data().length / 1000) + ")";
    }
}
