package me.olliejonas.saltmarsh.util;

import me.olliejonas.saltmarsh.music.Queueable;
import me.olliejonas.saltmarsh.music.entities.AudioQueue;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Testing {

    public record Int(int i) implements Queueable {

        public static Int of(int i) {
            return new Int(i);
        }

        @Override
        public String id() {
            return String.valueOf(i);
        }
    }

    private static class Listener implements AudioQueue.Listener<Int> {

        @Override
        public void onNextItem(Int next) {
            System.out.println("called from onSongChange (" + next.i() + ")");
        }

        @Override
        public void onQueueEmpty() {
            System.out.println("queue empty");
        }

        @Override
        public void onAdd(Int empty) {
        }
    }

    public static void main(String[] args) {
        AudioQueue<Int> queue = new AudioQueue<>();
        Listener listener = new Listener();
        queue.attachListener(listener);

        repeat(i -> {
            queue.add(Int.of(i * 2));
            print("queue (" + i + "): ", queue);
        }, 4);

        repeat(i -> {
            queue.next().ifPresentOrElse(j -> print("next (" + i + "): ", j), () ->
                    System.out.printf("next is null (%d)\n", i));
            queue.curr().ifPresentOrElse(j -> print("curr playing (" + i + "): ", j), () ->
                    System.out.printf("curr playing is null (%d)\n", i));
        }, 5);
    }

    private static void repeat(Runnable runnable, int times) {
        repeat(__ -> runnable.run(), times);
    }

    private static void repeat(Consumer<Integer> runnable, int times) {
        for (int i = 0; i < times; i++) runnable.accept(i);
    }

    private static void print(AudioQueue<Int> queue) {
        print("", queue);
    }

    private static void print(String before, AudioQueue<Int> queue) {
        System.out.println(before + queue.tracks().stream().map(Int::i).map(String::valueOf).collect(Collectors.joining(", ")) + " (" + queue.canImmediatelyPlay() + ")");
    }

    private static void print(Int i) {
        print("", i);
    }

    private static void print(String before, Int i) {
        System.out.println(before + i.i());
    }
}
