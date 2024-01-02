package me.olliejonas.saltmarsh.embed.wizard;

import lombok.Getter;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import me.olliejonas.saltmarsh.embed.wizard.types.StepRepeatingText;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
public class WizardEmbed {

    static MessageEmbed DEFAULT_COMPLETION_PAGE = GENERIC_COMPLETION_PAGE("Wizard");

    public static MessageEmbed GENERIC_COMPLETION_PAGE(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription("Thanks for completing this wizard!")
                .build();
    }

    static MessageEmbed EXIT_PAGE = new EmbedBuilder().setTitle("Wizard").setDescription("You have exited this wizard!").setColor(Color.RED).build();

    private final List<StepCandidate<?>> inputSteps;

    private final Function<Map<String, ?>, InteractionResponses> onCompletion;

    private final MessageEmbed completionPage;

    private final MessageEmbed exitPage;

    private final int noPages;

    private final AtomicInteger currentPageNo;

    // types have been checked before adding to this map - it's safe to just cast
    private final Map<String, Object> idToInputtedStepValueMap;

    private final boolean showExitButton;

    public static WizardEmbed.Builder builder() {
        return new Builder();
    }

    public WizardEmbed(List<StepCandidate<?>> inputSteps, Function<Map<String, ?>, InteractionResponses> onCompletion, MessageEmbed completionPage) {
        this(inputSteps, onCompletion, completionPage, EXIT_PAGE, true);
    }

    // ADD CONSUMER FOR ? FOR WHENEVER AN OPTION IS SELECT BEFORE ANYTHING ELSE HAPPENS
    // YOU COULD DO REPEATING STEPS LIKE THIS RATHER THAN CHECKING FOR TYPE IN THE LISTENER
    public WizardEmbed(List<StepCandidate<?>> inputSteps,
                       Function<Map<String, ?>, InteractionResponses> onCompletion,
                       MessageEmbed completionPage, MessageEmbed exitPage, boolean showExitButton) {
        this.inputSteps = inputSteps;

        this.noPages = inputSteps.size();
        this.currentPageNo = new AtomicInteger(0);

        this.idToInputtedStepValueMap = new HashMap<>();

        this.onCompletion = onCompletion;
        this.completionPage = completionPage;
        this.exitPage = exitPage;
        this.showExitButton = showExitButton;
    }


    // boolean is for whether they have completed it or not
    private Tuple2<Optional<StepCandidate<?>>, Boolean> next(int skip) {
        if (currentPageNo.get() + skip >= noPages) return new Tuple2<>(Optional.empty(), true);
        return new Tuple2<>(Optional.ofNullable(inputSteps.get(currentPageNo.addAndGet(skip))), false);
    }

    // first boolean represents whether the user is finished (true = is finished)
    // second tuple represents whether the current page was successful in assigning a value (true = was successful),
    // and an error message if not.
    @SuppressWarnings("unchecked")
    <T> Tuple4<Optional<StepCandidate<?>>, Boolean, Boolean, String> assignValueAndNext(Member sender, List<String> values,
                                                                                        StepCandidate.Method method,
                                                                                        @Nullable ActionComponent component) {
        StepCandidate<T> curr = (StepCandidate<T>) inputSteps.get(currentPageNo.get());

        for (String value : values) {
            // this is needed in-case the user decides to have a non-String based repeating text step.
            boolean isRepeatingTextButton = (curr instanceof StepRepeatingText<?> repeating &&
                    repeating.extraButtons().stream().anyMatch(button -> button.getLabel().equals(value)));

            Optional<T> convertedOpt = StringToTypeConverter.expandedCast(sender.getGuild(), value, curr.clazz());

            if (convertedOpt.isEmpty() && !isRepeatingTextButton)
                return new Tuple4<>(Optional.of(curr), false,
                        false, "I wasn't able to correctly turn your input into the correct type! Please try again!");  // couldn't cast successfully

            if (!isRepeatingTextButton && !curr.valid().test(convertedOpt.get(), curr).v1()) {
                Tuple2<Boolean, String> invalid = curr.valid().test(convertedOpt.get(), curr); // types get weird here hence no concat
                return new Tuple4<>(Optional.of(curr), false, invalid.v1(), invalid.v2());
            }

            boolean signaledRemoveLastInRepeatingText = (curr instanceof StepRepeatingText<?> repeating &&
                    component instanceof Button button &&
                    button.getLabel().equals(repeating.removePreviousItemText()));

            String id = curr.identifier();

            T converted = convertedOpt.orElse(null);

            if (curr instanceof StepRepeatingText<?> || values.size() != 1) {
                if (!idToInputtedStepValueMap.containsKey(id))
                    idToInputtedStepValueMap.put(id, new ArrayList<>());

                ArrayList<Object> storedValues = (ArrayList<Object>) idToInputtedStepValueMap.get(id);

                if (signaledRemoveLastInRepeatingText && !storedValues.isEmpty())
                    storedValues.remove(storedValues.size() - 1);

                if (!isRepeatingTextButton)
                    storedValues.add(converted);

            } else {
                idToInputtedStepValueMap.put(id, converted);
            }

            curr.onOption().accept(EntryContext.of(curr, converted, method, component));
        }

        return next(curr.skip()).concat(new Tuple2<>(true, ""));
    }

    public MessageCreateData toCreateData() {
        StepCandidate<?> curr = inputSteps.get(currentPageNo.get());
        return curr.compile(showExitButton);
    }

    public StepCandidate<?> curr() {
        return inputSteps.get(currentPageNo.get());
    }

    public InteractionResponses onCompletion() {
        return onCompletion.apply(idToInputtedStepValueMap);
    }

    public static class Builder {

        private final List<StepCandidate<?>> steps;

        private Function<Map<String, ?>, InteractionResponses> onCompletion;

        private MessageEmbed completionPage;

        private boolean showExitButton;

        public Builder() {
            this.steps = new ArrayList<>();
            this.onCompletion = (map) -> InteractionResponses.empty();
            this.completionPage = DEFAULT_COMPLETION_PAGE;
            this.showExitButton = true;
        }

        public Builder step(StepCandidate<?> step) {
            steps.add(step);
            return this;
        }

        public Builder steps(Collection<StepCandidate<?>> steps) {
            this.steps.addAll(steps);
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

        public WizardEmbed build() {
            return new WizardEmbed(steps, onCompletion, completionPage);
        }
    }
}
