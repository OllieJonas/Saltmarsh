package me.olliejonas.saltmarsh.util.functional;

import org.jooq.lambda.tuple.Tuple2;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface PredicateWithContext<T> {

    PredicateWithContext<?> IDENTITY_TRUE = __ -> new Tuple2<>(true, "");

    Tuple2<Boolean, String> test(T t);

    default PredicateWithContext<T> and(PredicateWithContext<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t).map1(bool -> bool && other.test(t).v1());
    }

    default PredicateWithContext<T> and(PredicateWithContext<? super T> other, String newMessage) {
        return (t) -> and(other).test(t).map2(str -> newMessage);
    }

    default Predicate<T> forget() {
        return t -> test(t).v1();
    }

    default PredicateWithContext<T> negate() {
        return (t) -> test(t).map1(result -> !result);
    }

    default PredicateWithContext<T> or(PredicateWithContext<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t).map1(bool -> bool && other.test(t).v1());
    }

    default PredicateWithContext<T> or(PredicateWithContext<? super T> other, String newMessage) {
        return (t) -> or(other).test(t).map2(str -> newMessage);
    }

    static <T> PredicateWithContext<T> from(Predicate<T> predicate, String message) {
        return (t) -> new Tuple2<>(predicate.test(t), message);
    }
}
