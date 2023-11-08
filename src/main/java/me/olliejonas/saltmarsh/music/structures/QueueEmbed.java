package me.olliejonas.saltmarsh.music.structures;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.embed.button.derivations.ItemizedEmbed;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbed;

import java.util.List;

public class QueueEmbed {

    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    private final AudioQueue<AudioTrack> tracks;

    private final int itemsPerPage;

    public QueueEmbed(AudioQueue<AudioTrack> tracks) {
        this(tracks, DEFAULT_ITEMS_PER_PAGE);
    }
    public QueueEmbed(AudioQueue<AudioTrack> tracks, int itemsPerPage) {
        this.tracks = tracks;
        this.itemsPerPage = itemsPerPage;
    }

    public PaginatedEmbed toPaginatedEmbed() {
        return ItemizedEmbed.builder()
                .title("Track Queue")
                .onEmpty("The queue is currently empty!")
                .items(toTracks())
                .itemsPerPage(itemsPerPage)
                .displayIndex()
                .build()
                .toPaginatedEmbed();
    }

    private List<TrackRepresentation> toTracks() {
        return tracks.stream().map(TrackRepresentation::new).toList();
    }
}
