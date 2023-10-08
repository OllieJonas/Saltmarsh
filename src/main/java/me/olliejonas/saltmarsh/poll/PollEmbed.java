package me.olliejonas.saltmarsh.poll;

import kotlin.jvm.functions.Function4;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;

import java.util.*;
import java.util.stream.Stream;

public record PollEmbed(PollEmbedManager manager, String question, String author, boolean singularVote,
                        boolean anonymous, List<PollOption> options,
                        Map<String, Integer> alreadyVoted) {

    private static final List<Button> OPTION_BUTTONS = Stream.of(
            "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"
    ).map(str -> Button.secondary("_", Emoji.fromUnicode(str))).toList();

    public static final Function4<PollEmbedManager, String, Boolean, Boolean, PollEmbed> YES_NO =
        (manager, q, anon, singular) -> builder(manager)
                .question(q)
                .options(List.of(new PollOption("Yes"), new PollOption("No")))
                .anonymous(anon)
                .singularVotes(singular)
                .build();

    public static final Function3<PollEmbedManager, String, Boolean, PollEmbed> YES_NO_SINGULAR = (manager, q, anon) -> YES_NO.invoke(manager, q, anon, true);

    public static final Function2<PollEmbedManager, String, PollEmbed> YES_NO_SINGULAR_VISIBLE = (manager, q) -> YES_NO.invoke(manager, q, false, true);
    public static Builder builder(PollEmbedManager manager) {
        return new Builder(manager);
    }

    public ButtonEmbed toEmbed() {
        EmbedBuilder embedBuilder = EmbedUtils.colour();
        embedBuilder.setTitle((anonymous ? "Anonymous " : "") + "Poll (created by " + author + ")");
        embedBuilder.setDescription(question);

        int i = 0;

        for (PollOption option : options) {

            Set<String> voters = option.voters();
            int size = voters.size();

            String title = Objects.requireNonNull(OPTION_BUTTONS.get(i++).getEmoji()).getAsReactionCode() +
                    "  " +
                    option.prompt();

            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append(size);
            descBuilder.append(" vote");

            if (size == 0 || size > 1) descBuilder.append("s");
            if (!anonymous) descBuilder.append("  ").append(option.votersString());

            embedBuilder.addField(title, descBuilder.toString(), false);
        }

        embedBuilder.setFooter("This poll will expire in " +
                WeakConcurrentHashMap.DEFAULT_EXPIRATION_TIME + " " +
                WeakConcurrentHashMap.DEFAULT_EXPIRATION_UNITS.name() +
                " from the time this message was sent!");

        ButtonEmbed.Builder builder = ButtonEmbed.builder(embedBuilder);

        i = 0;
        for (PollOption option : options) {
            builder.button(OPTION_BUTTONS.get(i++), clickContext -> {
                manager.get(clickContext.messageId()).ifPresent(embed -> {
                    embed.vote(clickContext.clicker(), clickContext.index());
                    clickContext.message().queue(this::update);
                });
                return InteractionResponses.messageAsEmbed("Thank you for voting! :)", true);
            });
        }
        return builder.build();
    }

    private void update(Message message) {
        message.editMessageEmbeds(toEmbed()).queue();
    }

    private void vote(Member clicker, int index) {
        String name = clicker.getEffectiveName();

        boolean voted = options.get(index).vote(name);

        if (singularVote) {
            if (alreadyVoted.containsKey(name) && alreadyVoted.get(name) != index) {
                options.get(alreadyVoted.get(name)).vote(name); // remove previous vote if they've voted
            }
            alreadyVoted.put(name, index);
        }
    }

    public static class Builder {

        private final PollEmbedManager manager;

        private String question;

        private String author;

        private boolean singularVotes;

        private boolean anonymous;

        private List<PollOption> options;

        public Builder(PollEmbedManager manager) {
            this.manager = manager;
            this.options = new ArrayList<>();
            this.singularVotes = true;
            this.anonymous = true;
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

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
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
            return new PollEmbed(manager,
                    question,
                    author,
                    singularVotes,
                    anonymous,
                    options,
                    new HashMap<>());
        }
    }
}
