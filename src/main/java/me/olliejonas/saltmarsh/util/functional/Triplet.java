package me.olliejonas.saltmarsh.util.functional;

import org.jooq.lambda.tuple.Tuple3;

public class Triplet<V1, V2, V3> extends Tuple3<V1, V2, V3> {

    public Triplet(Tuple3<V1, V2, V3> tuple) {
        super(tuple);
    }

    public Triplet(V1 v1, V2 v2, V3 v3) {
        super(v1, v2, v3);
    }

    public Triplet<V1, V2, V3> map3WithAll(TriFunction<V1, V2, V3, V3> mappingFunction) {
        return new Triplet<>(v1, v2, mappingFunction.apply(v1, v2, v3));
    }
}
