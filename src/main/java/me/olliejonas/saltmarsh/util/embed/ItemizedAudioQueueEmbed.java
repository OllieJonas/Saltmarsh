package me.olliejonas.saltmarsh.util.embed;

import lombok.experimental.UtilityClass;
import me.olliejonas.saltmarsh.music.entities.AudioQueue;
import me.olliejonas.saltmarsh.music.entities.Track;
import me.olliejonas.saltmarsh.util.RandomUtils;
import me.olliejonas.saltmarsh.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@UtilityClass
public class ItemizedAudioQueueEmbed {

    public PaginatedEmbed build(PaginatedEmbedManager manager, AudioQueue<Track> queue, int itemsPerPage) {
        List<EmbedBuilder> builders = toList(queue, itemsPerPage);
        int noPages = builders.size();
        AtomicInteger counter = new AtomicInteger(0);
        builders = builders.stream().map(builder -> builder.setFooter("Page " + counter.incrementAndGet() + " / " + noPages)).collect(Collectors.toList());

        PaginatedEmbed embed = PaginatedEmbed.builder().embeds(builders).build();
        embed.compile(manager);
        return embed;
    }

    private List<EmbedBuilder> toList(AudioQueue<Track> queue, int itemsPerPage) {
        List<List<String>> tracks = RandomUtils.batches(rows(queue.tracks()), itemsPerPage).toList();
        return tracks.stream().map(ItemizedAudioQueueEmbed::from).collect(Collectors.toList());
    }

    private EmbedBuilder from(List<String> page) {
        EmbedBuilder builder = EmbedUtils.colour(new EmbedBuilder());

        builder.setDescription(String.join("\n", page));
        return builder;
    }

    private List<String> rows(Queue<Track> tracks) {
        AtomicInteger count = new AtomicInteger(1);
        return tracks.stream().map(t -> count.getAndIncrement() + ". " + from(t)).collect(Collectors.toList());
    }

    private String from(Track track) {
        return track.data().author + " - " + track.data().title + " (" + TimeUtils.secondsToString(track.data().length / 1000) + ")";
    }
}
