package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbed;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public record PollEmbed(PollEmbedManager manager, String question, String author, boolean singularVote,
                        boolean anonymous, long expirationTime, TimeUnit expirationUnit, List<PollOption> options,
                        Map<String, Integer> alreadyVoted) {

    private static final long DEFAULT_EXPIRATION_TIME = 24;

    private static final TimeUnit DEFAULT_EXPIRATION_UNITS = TimeUnit.HOURS;

    private static final List<Button> OPTION_BUTTONS = Stream.of(
            "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"
    ).map(str -> Button.secondary("_", Emoji.fromUnicode(str))).toList();

    public static Builder builder(PollEmbedManager manager) {
        return new Builder(manager);
    }

    public ButtonEmbed toEmbed() {
        EmbedBuilder embedBuilder = EmbedUtils.colour();
        embedBuilder.setTitle("Poll (" + author + ")");
        embedBuilder.setDescription(question);


        for (PollOption option : options) {
            embedBuilder.addField(option.prompt(), option.voters().size() + " vote(s)", false);
        }

        embedBuilder.setFooter("This poll will expire in " + expirationTime + " " + expirationUnit.name() + " from the time this message was sent!");

        ButtonEmbed.Builder builder = ButtonEmbed.builder(embedBuilder);

        int i = 0;
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
        String clickerId = clicker.getId();
        boolean shouldRemove;

        boolean voted = options.get(index).vote(clickerId);

        if (singularVote) {
            if (alreadyVoted.containsKey(clickerId) && alreadyVoted.get(clickerId) != index) {
                options.get(alreadyVoted.get(clickerId)).vote(clickerId); // remove previous vote if they've voted
            }
            alreadyVoted.put(clickerId, index);
        }

    }


    public static class Builder {

        private final PollEmbedManager manager;

        private String question;

        private String author;

        private boolean singularVotes;

        private boolean anonymous;
        private long expirationTime;

        private TimeUnit expirationUnit;

        private List<PollOption> options;

        public Builder(PollEmbedManager manager) {
            this.manager = manager;
            this.options = new ArrayList<>();
            this.singularVotes = true;
            this.anonymous = true;
            this.expirationTime = DEFAULT_EXPIRATION_TIME;
            this.expirationUnit = DEFAULT_EXPIRATION_UNITS;
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder expirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder expirationUnit(TimeUnit expirationUnit) {
            this.expirationUnit = expirationUnit;
            return this;
        }

        public Builder singularVotes() {
            this.singularVotes = true;
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
                    expirationTime,
                    expirationUnit,
                    options,
                    new HashMap<>());
        }
    }
}
