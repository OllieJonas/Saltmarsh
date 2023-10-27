package me.olliejonas.saltmarsh.poll;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.SQLManager;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class PollManagerImpl extends SQLManager implements PollManager {

    @Getter
    private final ButtonEmbedManager manager;

    @Setter
    private Map<String, PollEmbed> embedMap;

    public PollManagerImpl(Connection connection, ButtonEmbedManager manager) {
        this(connection, manager, new HashMap<>());
    }

    public PollManagerImpl(Connection connection, ButtonEmbedManager manager, Map<String, PollEmbed> embedMap) {
        super(connection);
        this.manager = manager;
        this.embedMap = embedMap;
    }

    public InteractionResponses send(Member sender, TextChannel channel, PollEmbed embed, boolean notifyChannel) {
        if (notifyChannel)
            channel.sendMessage("@here " + sender.getEffectiveName() + " has sent a poll!").complete();

        return manager.register(toEmbed(embed), message -> registerEmbed(message, embed));
    }

    public Optional<PollEmbed> get(String messageId) {
        return Optional.of(embedMap.get(messageId));
    }

    public InteractionResponses vote(ButtonEmbed.ClickContext context) {
        AtomicReference<InteractionResponses> response = new AtomicReference<>(
                InteractionResponses.error("Poll failed! (should never appear)"));

        get(context.messageId()).ifPresent(embed -> {
            boolean singularVote = embed.singularVote();
            int index = context.index();

            Member clicker = context.clicker();

            String id = clicker.getId();
            String name = clicker.getEffectiveName();

            boolean voted = doVote(embed, id, name, index, singularVote);
            addVoteSql(context.messageId(), clicker.getGuild().getId(), id, index, voted, singularVote);

            context.message().queue(message -> message.editMessageEmbeds(toEmbed(embed)).queue());
            response.set(InteractionResponses.messageAsEmbed("Thank you for voting!", true));
        });

        return response.get();
    }

    void addToButtonMap(String messageId, PollEmbed embed) {
        manager.addToMap(messageId, toEmbed(embed));
    }

    boolean doVote(PollEmbed embed, String id, String name, int index, boolean singularVote) {
        Map<String, Integer> alreadyVoted = embed.alreadyVoted();

        if (singularVote) {
            if (alreadyVoted.containsKey(id) && alreadyVoted.get(id) != index) {
                embed.options().get(alreadyVoted.get(id)).vote(name); // remove previous vote if they've voted
            }
            alreadyVoted.put(id, index);
        }

        return embed.options().get(index).vote(name);
    }


    public ButtonEmbed toEmbed(PollEmbed embed) {
        String question = embed.question();
        List<PollOption> options = embed.options();
        String author = embed.author();
        boolean anonymous = embed.anonymous();
        boolean textRepresentation = embed.textRepresentation();

        EmbedBuilder embedBuilder = EmbedUtils.colour();
        embedBuilder.setTitle((anonymous ? "Anonymous " : "") + "Poll (created by " + author + ")");
        embedBuilder.setDescription(question);

        int i = 0;

        for (PollOption option : options) {

            Set<String> voters = option.voters();
            int size = voters.size();

            String title = (textRepresentation ? "" :
                    Objects.requireNonNull(PollEmbed.OPTION_BUTTONS.get(i++).getEmoji()).getAsReactionCode()) +
                    "  " +
                    option.prompt();

            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append(size);
            descBuilder.append(" vote");

            if (size == 0 || size > 1) descBuilder.append("s");
            if (!anonymous) descBuilder.append("  ").append(option.votersString());

            embedBuilder.addField(title, descBuilder.toString(), false);
        }

        ButtonEmbed.Builder builder = ButtonEmbed.builder(embedBuilder);

        i = 0;

        for (PollOption option : options) {
            Button button = PollEmbed.OPTION_BUTTONS.get(i++);

            if (textRepresentation) {
                String optText = option.prompt();
                optText = optText.length() >= PollEmbed.BUTTON_MAX_LENGTH ?
                        optText.substring(0, PollEmbed.BUTTON_MAX_LENGTH - 3) + "..." : optText;

                button = Button.primary("_", optText);
            }

            builder.button(button, this::vote);
        }

        return builder.build();
    }

    private void registerEmbed(Message message, PollEmbed embed) {
        embedMap.put(message.getId(), embed);
        addPollToSql(message, embed);
    }

    private void addPollToSql(Message message, PollEmbed embed) {
        String messageId = message.getId();
        String question = embed.question();
        String creator = embed.author();
        boolean anonymous = embed.anonymous();
        boolean singular = embed.singularVote();
        boolean textRepr = embed.textRepresentation();
        String options = embed.options().stream().map(PollOption::prompt).collect(Collectors.joining(PollManager.SQL_POLL_SPLIT));

        String statementStr = "INSERT INTO " + Constants.DB.POLLS +
                "(`message_id`, `question`, `creator`, `anonymous`, `singular`, `text_repr`, `options`) VALUES (?, ?, ?, ?, ?, ?, ?)";

        prepareStatement(statementStr).ifPresent(statement -> {
            try {
                statement.setString(1, messageId);
                statement.setString(2, question);
                statement.setString(3, creator);
                statement.setBoolean(4, anonymous);
                statement.setBoolean(5, singular);
                statement.setBoolean(6, textRepr);
                statement.setString(7, options);

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    // voteRemoved is when the person voted for the same option after already voting for it, meaning they have un-voted for it.
    // singular needs to remove anything where the message id, guild and the voter are the same. non-singular votes need to know the option (either to remove or add).
    // if it's singular, and a vote is added, we need 2 queries; delete the previous votes, and add the new one in.
    // if it's singular, and it's being removed, it doesn't really matter.
    public void addVoteSql(String messageId, String guild, String voter, int option, boolean voteRemoved, boolean singularVote) {

        String stmtStr = getVoteStmtStr(voteRemoved, singularVote);

        prepareStatement(stmtStr).ifPresent(statement -> {
            int i = 1;
            try {
                statement.setString(i++, messageId);
                statement.setString(i++, guild);
                statement.setString(i++, voter);

                if (!singularVote || voteRemoved)
                    statement.setInt(i++, option);
                else {
                    statement.setString(i++, messageId);
                    statement.setString(i++, guild);
                    statement.setString(i++, voter);
                    statement.setInt(i++, option);
                }

                statement.execute();
                statement.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    private static String getVoteStmtStr(boolean voteRemoved, boolean singularVote) {
        StringBuilder builder = new StringBuilder();

        if (voteRemoved || singularVote)
            builder.append("DELETE FROM " + Constants.DB.POLL_OPTIONS + " WHERE `message_id` = ? AND `guild` = ? AND `voter` = ?");

        if (singularVote && !voteRemoved)
            builder.append(";");

        if (voteRemoved)
            builder.append(" AND `option` = ?;");
        else
            builder.append("INSERT INTO " + Constants.DB.POLL_OPTIONS + "(`message_id`, `guild`, `voter`, `option`) VALUES (?, ?, ?, ?);");

        return builder.toString();
    }


}
