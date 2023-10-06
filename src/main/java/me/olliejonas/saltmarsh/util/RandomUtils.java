package me.olliejonas.saltmarsh.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomUtils {
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
}
