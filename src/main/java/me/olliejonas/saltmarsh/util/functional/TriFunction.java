package me.olliejonas.saltmarsh.util.functional;


import java.util.Objects;
import java.util.function.Function;

/**
 * Designed to replicate java.util.function's Function & BiFunction's functionality (haha), only extending to include
 * three inputs to one output.
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }
}
