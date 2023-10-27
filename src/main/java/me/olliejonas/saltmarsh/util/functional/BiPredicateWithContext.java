package me.olliejonas.saltmarsh.util.functional;

import org.jooq.lambda.tuple.Tuple2;

import java.util.Objects;
import java.util.function.BiPredicate;

@FunctionalInterface
public interface BiPredicateWithContext<T, U> {

    BiPredicateWithContext<?, ?> IDENTITY_TRUE = (__, ___) -> new Tuple2<>(true, "");

    Tuple2<Boolean, String> test(T t, U u);

    default BiPredicateWithContext<T, U> and(BiPredicateWithContext<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (t, u) -> test(t, u).map1(bool -> bool && other.test(t, u).v1());
    }

    default BiPredicateWithContext<T, U> and(BiPredicateWithContext<? super T, ? super U> other, String newMessage) {
        return (t, u) -> and(other).test(t, u).map2(str -> newMessage);
    }

    default BiPredicate<T, U> forget() {
        return (t, u) -> test(t, u).v1();
    }

    static <T, U> BiPredicateWithContext<T, U> from(BiPredicate<T, U> predicate, String message) {
        return (t, u) -> new Tuple2<>(predicate.test(t, u), message);
    }

    default BiPredicateWithContext<T, U> negate() {
        return (t, u) -> test(t, u).map1(result -> !result);
    }

    default BiPredicateWithContext<T, U> or(BiPredicateWithContext<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (t, u) -> test(t, u).map1(bool -> bool && other.test(t, u).v1());
    }

    default BiPredicateWithContext<T, U> or(BiPredicateWithContext<? super T, ? super U> other, String newMessage) {
        return (t, u) -> or(other).test(t, u).map2(str -> newMessage);
    }
}
