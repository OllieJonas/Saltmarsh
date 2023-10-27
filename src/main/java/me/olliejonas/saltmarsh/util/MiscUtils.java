package me.olliejonas.saltmarsh.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jooq.lambda.tuple.Tuple2;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MiscUtils {
    public static Optional<URL> url(String url) {
        try {
            return Optional.of(new URL(url));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    public static boolean betweenExclusive(int x, int min, int max) {
        return x >= min && x < max;
    }

    // https://stackoverflow.com/questions/12026885/is-there-a-common-java-utility-to-break-a-list-into-batches
    public static <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length (itemsPerPage) = " + length);
        int size = source.size();
        if (size <= 0)
            return Stream.empty();
        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    public static void printMap(String pfx, Map<String, String> map) {
        System.out.println(pfx + map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ")));
    }

    public static void logScheduledEvent(ScheduledEvent event) {
        System.out.println("Title: " + event.getName());
        System.out.println("Organiser: " + (event.getCreator() == null ? "null" : event.getCreator().getName()));
        System.out.println("Location: " + event.getLocation());
        System.out.println("Description: " + event.getDescription());
        System.out.println("Start Time: " + event.getStartTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.println("End Time: " + (event.getEndTime() == null ? "null" : event.getEndTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
        System.out.println("Status: " + event.getStatus().name());
        System.out.println("Type: " + event.getType().name());
    }

    public static void printNestedMap(String pfx, Map<String, Map<String, Tuple2<String, String>>> map) {
        System.out.println(pfx + map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().entrySet().stream().map(e1 -> e1.getKey() + ": " + e1.getValue()).collect(Collectors.joining(", ", "{", "}"))).collect(Collectors.joining(", ", "{", "}")));
    }

    public static ImageProxy getMostRelevantAvatar(Member member) {
        return member.getAvatar() == null ? member.getUser().getAvatar() : member.getAvatar();
    }
}
