package me.olliejonas.saltmarsh.music.entities;

import org.jetbrains.annotations.NotNull;

public enum AudioSource {
    SPOTIFY("Spotify"),
    YOUTUBE("YouTube"),
    SOUNDCLOUD("Soundcloud");


    private final String title;

    AudioSource(String title) {
        this.title = title;
    }

    @NotNull
    public String title() {
        return title;
    }
}
