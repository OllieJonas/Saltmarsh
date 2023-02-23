package me.olliejonas.saltmarsh.util.functional;

import org.jooq.lambda.tuple.Tuple2;

import java.util.function.BiFunction;

public class Pair<V1, V2> extends Tuple2<V1, V2> {


    public Pair(Tuple2<V1, V2> tuple) {
        super(tuple);
    }

    public static <V1, V2> Pair<V1, V2> of(Tuple2<V1, V2> tuple) {
        return new Pair<>(tuple);
    }

    public Pair(V1 v1, V2 v2) {
        super(v1, v2);
    }

    public static <V1, V2> Pair<V1, V2> of(V1 v1, V2 v2) {
        return new Pair<>(v1, v2);
    }

    public Pair<V1, V2> map2WithAll(BiFunction<V1, V2, V2> mappingFunction) {
        return new Pair<>(v1, mappingFunction.apply(v1, v2));
    }
}
