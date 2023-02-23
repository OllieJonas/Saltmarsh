package me.olliejonas.saltmarsh.util.structures;

public interface WeakConcurrentHashMapListener<K,V> {
    void notifyOnAdd(K key, V value);
    void notifyOnRemoval(K key, V value);
}
