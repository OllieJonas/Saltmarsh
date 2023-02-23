package me.olliejonas.saltmarsh.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomUtils {

    public static boolean betweenExclusive(int x, int min, int max) {
        return x >= min && x < max;
    }

    // https://stackoverflow.com/questions/12026885/is-there-a-common-java-utility-to-break-a-list-into-batches
    public static <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length = " + length);
        int size = source.size();
        if (size <= 0)
            return Stream.empty();
        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }

    // regex courtesy of chatgpt xx (not im joking, god recent times are scary)
    // group 1 = protocol (http(s)), group 2 = subdomain, group 3 = domain, 4 = path
    private static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?(?:([\\w\\-]+)\\.)?([\\w\\-]+(?:\\.[\\w\\-]+)+)([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$");

    public static Matcher url(String url) {
        return URL_PATTERN.matcher(url);
    }

    public static boolean isUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}
