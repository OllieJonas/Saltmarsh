package me.olliejonas.saltmarsh.embed.wizard;

import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.Nullable;

// component is null if it's a text menu, not null for everything else
public record EntryContext<T>(StepCandidate<T> self, T result, StepCandidate.Method method, @Nullable ActionComponent component) {

    public static <T> EntryContext<T> of(StepCandidate<T> self, T result, StepCandidate.Method method, ActionComponent component) {
        return new EntryContext<>(self, result, method, component);
    }


}
