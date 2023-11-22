package me.olliejonas.saltmarsh.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FluentMapBuilder<K, V> {

    public static <K, V> FluentMapBuilder<K, V> builder() {
        return builder(new HashMap<>());
    }

    public static <K, V> FluentMapBuilder<K, V> builder(Map<K, V> map) {
        return new FluentMapBuilder<>(map);
    }

    private final Map<K, V> map;
    public FluentMapBuilder<K, V> add(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return map;
    }
}
