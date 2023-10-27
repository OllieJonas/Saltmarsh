package me.olliejonas.saltmarsh.poll;

import kotlin.jvm.functions.Function4;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record PollEmbed(String question, String author, boolean singularVote,
                        boolean anonymous, boolean textRepresentation, List<PollOption> options,
                        Map<String, Integer> alreadyVoted) {

    static int BUTTON_MAX_LENGTH = 80;

    public static final List<Button> OPTION_BUTTONS = Stream.of(
            "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"
    ).map(str -> Button.secondary("_", Emoji.fromUnicode(str))).toList();

    public static final Function4<PollManager, String, Boolean, Boolean, PollEmbed> YES_NO =
        (manager, q, anon, singular) -> builder()
                .question(q)
                .options(List.of(new PollOption("Yes"), new PollOption("No")))
                .anonymous(anon)
                .singularVotes(singular)
                .build();

    public static final Function3<PollManager, String, Boolean, PollEmbed> YES_NO_SINGULAR = (manager, q, anon) -> YES_NO.invoke(manager, q, anon, true);

    public static final Function2<PollManager, String, PollEmbed> YES_NO_SINGULAR_VISIBLE = (manager, q) -> YES_NO.invoke(manager, q, false, true);
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String question;

        private String author;

        private boolean singularVotes;

        private boolean anonymous;

        private boolean textRepresented;

        private List<PollOption> options;

        public Builder() {
            this.options = new ArrayList<>();
            this.singularVotes = false;
            this.anonymous = false;
            this.textRepresented = false;
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder singularVotes() {
            return singularVotes(true);
        }

        public Builder singularVotes(boolean singularVotes) {
            this.singularVotes = singularVotes;
            return this;
        }

        public Builder anonymous() {
            return anonymous(true);
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Builder textRepresented() {
            return textRepresented(true);
        }

        public Builder textRepresented(boolean textRepresented) {
            this.textRepresented = textRepresented;
            return this;
        }

        public Builder option(String option) {
            return option(new PollOption(option));
        }

        public Builder option(PollOption option) {
            this.options.add(option);
            return this;
        }

        public Builder optionsStr(List<String> options) {
            return options(options.stream().map(PollOption::new).toList());
        }

        public Builder options(List<PollOption> options) {
            this.options = options;
            return this;
        }

        public PollEmbed build() {
            return new PollEmbed(
                    question,
                    author,
                    singularVotes,
                    anonymous,
                    textRepresented,
                    options,
                    new HashMap<>());
        }
    }
}
