package me.olliejonas.saltmarsh.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BiMap<K,V> extends AbstractMap<K, V> {

    private final Map<K, V> map;

    private final Map<V, Set<K>> inversedMap;

    public BiMap() {
        this.map = new HashMap<>();
        this.inversedMap = new HashMap<>();
    }

    public V put(K k, V v) {
        map.put(k, v);

        if (!inversedMap.containsKey(v))
            inversedMap.put(v, new HashSet<>());

        inversedMap.get(v).add(k);
        return v;
    }

    public V get(Object k) {
        return map.get(k);
    }

    public K getKey(V v) {
        Set<K> keys = getKeys(v);

        if (keys == null) return null;

        return keys.stream().findFirst().orElse(null);
    }

    public Set<K> getKeys(V v) {
        return inversedMap.get(v);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
