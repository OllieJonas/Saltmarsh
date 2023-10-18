package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.embed.input.types.InputCandidate;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.Nullable;

// component is null if it's a text menu, not null for everything else
public record EntryContext<T>(InputCandidate<T> self, T result, InputCandidate.Method method, @Nullable ActionComponent component) {

    public static <T> EntryContext<T> of(InputCandidate<T> self, T result, InputCandidate.Method method, ActionComponent component) {
        return new EntryContext<>(self, result, method, component);
    }


}
