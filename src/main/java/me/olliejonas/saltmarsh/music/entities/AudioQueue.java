package me.olliejonas.saltmarsh.music.entities;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.music.Queueable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

// generics to support testing (should always be used as track in prod)
public class AudioQueue<E extends Queueable> {

    public interface Listener<E> {

        void onNextItem(E next);
        void onQueueEmpty();

        default void onAdd(E item) {}
    }

    private final Queue<E> queue;

    private E currentlyPlaying;

    @Getter
    private final Set<Listener<E>> listeners;

    @Setter
    private boolean repeating = false;

    public AudioQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.listeners = new HashSet<>();

        this.currentlyPlaying = null;
    }

    public void add(E item) {
        this.queue.add(item);
        listeners.forEach(l -> l.onAdd(item));
    }

    public void add(Collection<E> track) {
        for (E queueable : track) {
            add(queueable);
        }
    }
    public Optional<E> curr() {
        return Optional.ofNullable(currentlyPlaying);
    }

    public void curr(E item) {
        this.currentlyPlaying = item;
    }

    public boolean canImmediatelyPlay() {
        return currentlyPlaying == null;
    }

    public Optional<E> next() {
        if (repeating) return Optional.of(currentlyPlaying);

        E next = queue.poll();
        curr(next);

        // nothing left in the queue
        if (next == null) {
            this.listeners.forEach(Listener::onQueueEmpty);
            return Optional.empty();
        } else {
            return Optional.of(next);
        }
    }

    public Queue<E> tracks() {
        return queue;
    }

    public void remove(E queueable) {
        queue.remove(queueable);
        if (queueable.id().equals(currentlyPlaying.id())) {
            currentlyPlaying = queue.poll();
        }
    }

    public boolean toggleRepeating() {
        this.repeating = !this.repeating;
        return this.repeating;
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public AudioQueue<E> shuffle() {
        List<E> shuffled = new ArrayList<>(queue.stream().toList());
        Collections.shuffle(shuffled);
        queue.clear();
        queue.addAll(shuffled);
        return this;
    }

    public void attachListener(Listener<E> listener) {
        listeners.add(listener);
    }
}
