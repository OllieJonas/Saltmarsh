package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BooleanSupplier;

public interface InputTargetEmbed {

    default Collection<BooleanSupplier> predicates() {
        return Collections.emptySet();
    }

    void compile(InputEmbedManager manager);

    default int skip() {
        return 1;
    }
}
