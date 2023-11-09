package me.olliejonas.saltmarsh.music;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpotifyTransformer {

    static final Logger LOGGER = LoggerFactory.getLogger(SpotifyTransformer.class);

    private final SpotifyApi api;

    // returns list of yt searches, if unable to parse returns null.
    // supports single tracks (singleton list), albums and playlists.

    public SpotifyTransformer(SpotifyApi api) {
        this.api = api;
    }

    // if null, then api isn't enabled. if emptyList, then wasn't able to determine track / album / playlist
    // (i.e. not supported on spotify).
    public List<String> transform(String link, int limit) throws IOException, ParseException, SpotifyWebApiException {
        if (api == null)
            return null;

        String[] replaced = link.replace("https://open.spotify.com/", "").split("/");
        String type = replaced[0];
        String id = replaced[1].split("\\?")[0];

        LOGGER.info(id);

        return switch(type) {
            case "track" -> Collections.singletonList(single(id));
            case "playlist" -> playlist(id, limit);
            case "album" -> album(id, limit);
            default -> Collections.emptyList();
        };
    }

    private String single(String id) throws IOException, ParseException, SpotifyWebApiException {
        Track track = api.getTrack(id).build().execute();
        return toSearchQuery(track.getArtists(), track.getName());
    }

    private List<String> album(String id, int limit) throws IOException, ParseException, SpotifyWebApiException {
        Paging<TrackSimplified> trackPaging = api.getAlbumsTracks(id).limit(limit).build().execute();
        return Arrays.stream(trackPaging.getItems())
                .map(track -> toSearchQuery(track.getArtists(), track.getName()))
                .toList();
    }

    private List<String> playlist(String id, int limit) throws IOException, ParseException, SpotifyWebApiException {
        Paging<PlaylistTrack> trackPaging = api.getPlaylistsItems(id).limit(limit).build().execute();
        return Arrays.stream(trackPaging.getItems())
                .map(PlaylistTrack::getTrack)
                .filter(track -> track instanceof Track)
                .map(track -> (Track) track)
                .map(track -> toSearchQuery(track.getArtists(), track.getName()))
                .toList();
    }

    private String toSearchQuery(ArtistSimplified[] artists, String name) {
        return Arrays.stream(artists)
                .map(ArtistSimplified::getName)
                .collect(Collectors.joining(", ")) + " - " + name;
    }

    private Stream<String> withYtSearch(Stream<String> tracks) {
        return tracks.map(track -> "ytsearch:" + track);
    }
}
