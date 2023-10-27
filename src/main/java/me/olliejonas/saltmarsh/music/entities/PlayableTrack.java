package me.olliejonas.saltmarsh.music.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.embed.button.derivations.Itemizable;
import me.olliejonas.saltmarsh.music.Queueable;
import net.dv8tion.jda.api.entities.User;

public record PlayableTrack(AudioTrack track, User owner, TrackInfo info) implements Queueable, Itemizable {


    public static PlayableTrack of(AudioTrack track, User owner) {
        return new PlayableTrack(track, owner, TrackInfo.from(track));
    }

    @Override
    public String id() {
        return track.getIdentifier();
    }

    @Override
    public String representation() {
        return info.representation();
    }
}
