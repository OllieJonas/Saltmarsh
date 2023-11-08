package me.olliejonas.saltmarsh.music.structures;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AudioQueue<E> {

    @Setter
    private LinkedList<E> queue;

    @Setter
    @Getter
    // repeat the current item
    private boolean singleRepeating;

    @Setter
    private boolean cyclic;

    public AudioQueue() {
        this(Collections.emptyList());
    }

    public AudioQueue(Collection<E> elems) {
        this.queue = new LinkedList<>(elems);
        this.singleRepeating = false;
        this.cyclic = false;
    }

    public void add(E elem) {
        offer(elem);
    }

    public void add(Collection<E> elems) {
        offer(elems);
    }

    public void insert(int index, E elem) {
        queue.add(index, elem);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void offer(E elem) {
        queue.addLast(elem);
    }

    public void offer(Collection<E> elems) {
        for (E elem : elems) {
            offer(elem);
        }
    }

    public E peek() {
        return queue.peekFirst();
    }

    public E poll() {
        return poll(false);
    }

    public E poll(boolean overrideRepeating) {
        if (singleRepeating && !overrideRepeating) return peek();

        E elem = queue.pollFirst();

        if (cyclic)
            offer(elem);

        return elem;
    }

    public AudioQueue<E> shuffle() {
        Collections.shuffle(queue);
        return this;
    }

    public int size() {
        return queue.size();
    }

    public E skip(int amount) {
        if (amount <= 0) return peek();

        E curr = null;
        int i = 0;

        while (i++ < amount) {
            curr = poll(true);
        }

        return curr;
    }

    public Stream<E> stream() {
        return queue.stream();
    }

    public boolean toggleCyclic() {
        return this.cyclic = !this.cyclic;
    }

    public boolean toggleRepeating() {
        return this.singleRepeating = !this.singleRepeating;
    }

    public Collection<E> tracks() {
        return queue;
    }

    public void print() {
        System.out.println(queue.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
