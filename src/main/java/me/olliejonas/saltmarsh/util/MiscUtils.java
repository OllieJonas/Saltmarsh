package me.olliejonas.saltmarsh.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jooq.lambda.tuple.Tuple2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
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

    public static Member getMemberById(Guild guild, String id) {
        Member member = guild.getMemberById(id);

        return member != null ? member : guild.retrieveMemberById(id).complete();
    }

    public static Collector<CharSequence, ?, String> joinWithAnd() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    int size = list.size();
                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < size; i++) {
                            builder.append(list.get(i));

                            if (i != size - 1)
                                builder.append(i == size - 2 ? " and " : ", ");
                    }

                    return builder.toString();
                });
    }

    public static <T> Function<T, T> peek(Consumer<T> fn) {
        return (t) -> {
            fn.accept(t);
            return t;
        };
    }

    public static String getMessageLink(Message message) {
        String id = message.getId();
        String channelId = message.getChannelId();
        String guildId = message.getGuildId();

        return "https://discord.com/channels/" + guildId + "/" + channelId + "/" + id;
    }

    public static String shortenedStackTrace(Exception e, int maxLines) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));

        String[] lines = writer.toString().split("\n");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
            sb.append(lines[i]).append("\n");
        }

        return sb.toString();
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
