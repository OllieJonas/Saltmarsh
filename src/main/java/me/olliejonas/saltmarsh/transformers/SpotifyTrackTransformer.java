package me.olliejonas.saltmarsh.transformers;

import java.util.Set;

// one per guild
public class SpotifyTrackTransformer implements ITransformer {

    // taking advantage of the Spotify Batch API to reduce calls
    // request a bunch at the same time
    private Set<String> batchedTracks;

    public SpotifyTrackTransformer() {

    }

    public void transform(String string) {

    }
}
