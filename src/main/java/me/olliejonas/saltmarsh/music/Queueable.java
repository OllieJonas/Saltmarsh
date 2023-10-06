package me.olliejonas.saltmarsh.music;

public interface Queueable {
    String id();

    default void onQueued() {}
}
