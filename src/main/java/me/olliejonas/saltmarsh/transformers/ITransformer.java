package me.olliejonas.saltmarsh.transformers;

// lavaplayer only really supports YouTube, so for other music streaming services (eg. Spotify, TIDAL, etc.), we need to
// transform it into a YouTube track. Only real way to do this is to YT search for the trackname and pick the first
// search result. kinda sucks (& will be v prone to breaking), but here we are.
public interface ITransformer {
}
