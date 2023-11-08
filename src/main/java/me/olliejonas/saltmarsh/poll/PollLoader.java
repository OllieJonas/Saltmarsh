package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.Constants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PollLoader extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollLoader.class);

    private final PollManagerImpl manager;

    private final Connection connection;

    public PollLoader(PollManager manager, Connection connection) {
        this.manager = (PollManagerImpl) manager;
        this.connection = connection;
    }
    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();
        updatePollManager(jda);
    }

    private void updatePollManager(JDA jda) {
        if (connection == null) return;

        Map<String, PollEmbed> pollEmbedMap = new HashMap<>();
        boolean singularVote = false;

        try (PreparedStatement pollStatement = connection.prepareStatement("SELECT * FROM " + Constants.DB.POLLS + ";")) {
            ResultSet results = pollStatement.executeQuery();

            if (results == null) throw new IllegalStateException("resultSet for pollStatement is null!");

            while (results.next()) {
                String id = results.getString("message_id");
                String question = results.getString("question");
                String creator = results.getString("creator");
                boolean anonymous = results.getBoolean("anonymous");
                singularVote = results.getBoolean("singular");
                boolean textRepresentation = results.getBoolean("text_repr");
                List<PollOption> options = Arrays.stream(results.getString("options").split(PollManager.SQL_POLL_SPLIT)).map(PollOption::new).toList();

                pollEmbedMap.put(id, new PollEmbed(question, creator, singularVote, anonymous, textRepresentation, options, new HashMap<>()));
            }

        } catch (SQLException e) {
            LOGGER.warn("Unable to load poll info! Reverting to no loads", e);
            return;
        }

        manager.setEmbedMap(pollEmbedMap);

        try (PreparedStatement optionsStatement = connection.prepareStatement("SELECT * FROM " + Constants.DB.POLL_OPTIONS + ";")) {
            ResultSet results = optionsStatement.executeQuery();

            if (results == null) throw new IllegalStateException("resultSet for pollOptionsStatement is null!");

            while (results.next()) {
                String id = results.getString("message_id");
                String guildStr = results.getString("guild");
                String voterStr = results.getString("voter");
                int option = results.getInt("option");

                if (!pollEmbedMap.containsKey(id)) throw new IllegalStateException("Poll ID " + id + " isn't valid!");

                Guild guild = Objects.requireNonNull(jda.getGuildById(guildStr));
                Member voter = Objects.requireNonNull(guild.retrieveMemberById(voterStr).complete());

                manager.doVote(pollEmbedMap.get(id), voter.getId(), voter.getEffectiveName(), option, singularVote);
            }

            results.close();
        } catch (Exception e) {
            LOGGER.warn("Unable to retrieve poll votes in SQL! Reverting to no loads (of polls either)", e);
        }

        pollEmbedMap.forEach(manager::addToButtonMap);
    }
}
