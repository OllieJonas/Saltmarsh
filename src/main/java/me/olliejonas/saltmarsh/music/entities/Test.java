package me.olliejonas.saltmarsh.music.entities;

import me.olliejonas.saltmarsh.music.Queueable;

import java.util.List;
import java.util.stream.IntStream;

public class Test {

    record Q(String id) implements Queueable {}



    public static void main(String[] args) {
        List<Q> qs = IntStream.rangeClosed(0, 10).boxed().map(String::valueOf).map(Q::new).toList();
        AudioQueue<Q> queue = new AudioQueue<>();
        queue.add(qs);
    }
}
