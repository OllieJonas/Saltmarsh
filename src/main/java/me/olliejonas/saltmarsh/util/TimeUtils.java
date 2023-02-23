package me.olliejonas.saltmarsh.util;

import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple2;

@UtilityClass
public class TimeUtils {

    public Tuple2<Integer, Integer> toMinutesAndSeconds(long seconds) {
        return new Tuple2<>((int) seconds / 60, (int) seconds % 60);
    }

    public String secondsToString(long seconds) {
        Tuple2<Integer, Integer> minuteSeconds = toMinutesAndSeconds(seconds);
        return minuteSeconds.v1() + ":" + (minuteSeconds.v2() < 10 ? "0" : "") + minuteSeconds.v2();
    }

}
