package me.olliejonas.saltmarsh.transformers;

import java.util.function.Supplier;

public class SpotifyPlaylistTransformer {

    private final Supplier<ITransformer> trackTransformer;

    public SpotifyPlaylistTransformer() {
        this(SpotifyTrackTransformer::new);
    }

    public SpotifyPlaylistTransformer(Supplier<ITransformer> trackTransformer) {
        this.trackTransformer = trackTransformer;
    }
}
