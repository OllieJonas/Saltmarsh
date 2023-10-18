package me.olliejonas.saltmarsh.embed.input;

import lombok.Getter;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.input.types.InputCandidate;
import me.olliejonas.saltmarsh.embed.input.types.InputRepeatingText;
import me.olliejonas.saltmarsh.embed.input.types.InputText;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
public class InputEmbed {

    static MessageEmbed DEFAULT_COMPLETION_PAGE = GENERIC_COMPLETION_PAGE("Wizard");

    public static MessageEmbed GENERIC_COMPLETION_PAGE(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription("Thanks for completing this wizard!")
                .build();
    }

    static MessageEmbed EXIT_PAGE = new EmbedBuilder().setTitle("Exited").setDescription("You have exited this menu!").setColor(Color.RED).build();

    private final List<InputCandidate<?>> inputSteps;

    private final Function<Map<String, ?>, InteractionResponses> onCompletion;

    private final MessageEmbed completionPage;

    private final MessageEmbed exitPage;

    private final int noPages;

    private final AtomicInteger currentPageNo;

    // types have been checked before adding to this map - it's safe to just cast
    private final Map<String, Object> identifierToValueMap;

    private final boolean showExitButton;

    public static InputEmbed.Builder builder() {
        return new Builder();
    }

    public InputEmbed(List<InputCandidate<?>> inputSteps, Function<Map<String, ?>, InteractionResponses> onCompletion, MessageEmbed completionPage) {
        this(inputSteps, onCompletion, completionPage, EXIT_PAGE, true);
    }

    // ADD CONSUMER FOR ? FOR WHENEVER AN OPTION IS SELECT BEFORE ANYTHING ELSE HAPPENS
    // YOU COULD DO REPEATING STEPS LIKE THIS RATHER THAN CHECKING FOR TYPE IN THE LISTENER
    public InputEmbed(List<InputCandidate<?>> inputSteps,
                      Function<Map<String, ?>, InteractionResponses> onCompletion,
                      MessageEmbed completionPage, MessageEmbed exitPage, boolean showExitButton) {
        this.inputSteps = inputSteps;

        this.noPages = inputSteps.size();
        this.currentPageNo = new AtomicInteger(0);

        this.identifierToValueMap = new HashMap<>();

        this.onCompletion = onCompletion;
        this.completionPage = completionPage;
        this.exitPage = exitPage;
        this.showExitButton = showExitButton;
    }


    // boolean is for whether they have completed it or not
    private Tuple2<Optional<InputCandidate<?>>, Boolean> next(int skip) {
        if (currentPageNo.get() + skip >= noPages) return new Tuple2<>(Optional.empty(), true);

        return new Tuple2<>(Optional.ofNullable(inputSteps.get(currentPageNo.addAndGet(skip))), false);
    }

    // first boolean represents whether the user is finished (true = is finished)
    // second boolean represents whether the current page was successful in assigning a value (true = was successful).
    @SuppressWarnings("unchecked")
    <T> Tuple3<Optional<InputCandidate<?>>, Boolean, Boolean> assignValueAndNext(Member sender, List<String> values,
                                                                                        InputCandidate.Method method,
                                                                                        @Nullable ActionComponent component) {
        InputCandidate<T> curr = (InputCandidate<T>) inputSteps.get(currentPageNo.get());

        for (String value : values) {
            Optional<T> convertedOpt = StringToTypeConverter.expandedCast(sender.getGuild(), value, curr.clazz());
            if (convertedOpt.isEmpty() || !curr.valid().test(convertedOpt.get()))
                return new Tuple3<>(Optional.of(curr), false, false);  // couldn't cast successfully

            String id = curr.identifier();
            T converted = convertedOpt.get();

            curr.onOption().accept(EntryContext.of(curr, converted, method, component));

            if (curr instanceof InputRepeatingText<?> || values.size() != 1) {
                if (!identifierToValueMap.containsKey(id))
                    identifierToValueMap.put(id, new ArrayList<>());

                ((ArrayList<Object>) identifierToValueMap.get(id)).add(converted);
            } else {
                identifierToValueMap.put(id, converted);
            }
        }

        return next(curr.skip()).concat(true);
    }

    public MessageCreateData toCreateData() {
        InputCandidate<?> curr = inputSteps.get(currentPageNo.get());
        return curr.compile(showExitButton);
    }

    public boolean expectingInteraction() {
        return curr() instanceof InputRepeatingText<?> || curr() instanceof InputText<?>;
    }

    public InputCandidate<?> curr() {
        return inputSteps.get(currentPageNo.get());
    }

    public InteractionResponses onCompletion() {
        return onCompletion.apply(identifierToValueMap);
    }

    public static class Builder {

        private List<InputCandidate<?>> candidates;

        private Function<Map<String, ?>, InteractionResponses> onCompletion;

        private MessageEmbed completionPage;

        private boolean showExitButton;

        public Builder() {
            this.candidates = new ArrayList<>();
            this.onCompletion = (map) -> InteractionResponses.empty();
            this.completionPage = DEFAULT_COMPLETION_PAGE;
            this.showExitButton = true;
        }

        public Builder step(InputCandidate<?> step) {
            candidates.add(step);
            return this;
        }

        public Builder candidate(InputCandidate<?> candidate) {
            return step(candidate);
        }

        public Builder candidates(List<InputCandidate<?>> candidates) {
            this.candidates = candidates;
            return this;
        }

        public Builder onCompletion(Function<Map<String, ?>, InteractionResponses> onCompletion) {
            this.onCompletion = onCompletion;
            return this;
        }

        public Builder completionPage(MessageEmbed embed) {
            this.completionPage = embed;
            return this;
        }

        public Builder showExitButton(boolean showExitButton) {
            this.showExitButton = showExitButton;
            return this;
        }

        public Builder disableExitButton() {
            return showExitButton(false);
        }

        public InputEmbed build() {
            return new InputEmbed(candidates, onCompletion, completionPage);
        }
    }
}
