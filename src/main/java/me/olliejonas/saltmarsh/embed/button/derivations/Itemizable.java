package me.olliejonas.saltmarsh.embed.button.derivations;

import org.jetbrains.annotations.NotNull;

public interface Itemizable {

    // separated by "-" for fields
    String representation();


    record Strings(String representation) implements Itemizable, Comparable<Strings> {

        public int compareTo(@NotNull Strings other) {
            return representation.compareTo(other.representation());
        }
    }
}
