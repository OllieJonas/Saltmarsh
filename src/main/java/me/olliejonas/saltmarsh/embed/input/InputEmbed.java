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
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
public class InputEmbed {

    static MessageEmbed DEFAULT_COMPLETION_PAGE = new EmbedBuilder().setTitle("Done!").setDescription("Thanks!").build();
    private final List<InputCandidate<?>> inputSteps;

    private final Function<Map<String, ?>, InteractionResponses> onCompletion;

    private final MessageEmbed completionPage;

    private final int noPages;

    private final AtomicInteger currentPageNo;

    // types have been checked before adding to this map - it's safe to just cast
    private final Map<String, Object> identifierToValueMap;

    public static InputEmbed.Builder builder() {
        return new Builder();
    }

    public InputEmbed(List<InputCandidate<?>> inputSteps, Function<Map<String, ?>, InteractionResponses> onCompletion, MessageEmbed completionPage) {
        this.inputSteps = inputSteps;

        this.noPages = inputSteps.size();
        this.currentPageNo = new AtomicInteger(0);

        this.identifierToValueMap = new HashMap<>();

        this.onCompletion = onCompletion;
        this.completionPage = completionPage;
    }


    // boolean is for whether they have completed it or not
    public Tuple2<Optional<InputCandidate<?>>, Boolean> next(int skip, InputCandidate.Method method, String text) {
        if (currentPageNo.get() + 1 >= noPages) return new Tuple2<>(Optional.empty(), true);

        if (inputSteps.get(currentPageNo.get()).getClass() == InputRepeatingText.class
                && (method == InputCandidate.Method.BUTTON)) {
            System.out.println("button in repeating!");
            skip = 1;
        }

        return new Tuple2<>(Optional.ofNullable(inputSteps.get(currentPageNo.addAndGet(skip))), false);
    }

    // first boolean represents whether the user is finished
    // second boolean represents whether the current page was successful in assigning a value.
    @SuppressWarnings("unchecked")
    public Tuple3<Optional<InputCandidate<?>>, Boolean, Boolean> assignValueAndNext(Member sender, String value,
                                                                                    InputCandidate.Method method) {
        InputCandidate<?> curr = inputSteps.get(currentPageNo.get());

        Optional<?> converted = StringToTypeConverter.expandedCast(sender.getGuild(), value, curr.clazz());
        if (converted.isEmpty()) return new Tuple3<>(Optional.of(curr), false, false);  // couldn't cast successfully

        String id = curr.identifier();

        if (curr instanceof InputRepeatingText<?>) {
            if (!identifierToValueMap.containsKey(id))
                identifierToValueMap.put(id, new ArrayList<>());

            ((ArrayList<Object>) identifierToValueMap.get(id)).add(converted.get());
        } else {
            identifierToValueMap.put(id, converted.get());  // assign successfully cast
        }

        return next(curr.skip().apply(value), method, value).concat(true);
    }

    public MessageCreateData toEmbed() {
        InputCandidate<?> curr = inputSteps.get(currentPageNo.get());
        return curr.compile();
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

        public Builder() {
            this.candidates = new ArrayList<>();
            this.onCompletion = (map) -> InteractionResponses.empty();
            this.completionPage = DEFAULT_COMPLETION_PAGE;
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

        public InputEmbed build() {
            return new InputEmbed(candidates, onCompletion, completionPage);
        }
    }
}
