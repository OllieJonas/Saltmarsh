package me.olliejonas.saltmarsh.music.entities;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.music.GuildAudioManager;

public class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

    private GuildAudioManager manager;

    public AudioLoadResultHandlerImpl(GuildAudioManager manager) {
        this.manager = manager;
    }

    @Override
    public void trackLoaded(AudioTrack track) {

    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException exception) {

    }
}
