package me.olliejonas.saltmarsh.scheduledevents.recurring;

import me.olliejonas.saltmarsh.Constants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RecurringEventLoader extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecurringEventLoader.class);

    private final RecurringEventManagerImpl manager;

    private final Connection connection;
    public RecurringEventLoader(RecurringEventManager manager, Connection connection) {
        this.manager = (RecurringEventManagerImpl) manager;
        this.connection = connection;
    }

    @Override
    public void onReady(ReadyEvent readyEvent) {
        if (connection == null) return;

        manager.setConnection(connection);

        JDA jda = readyEvent.getJDA();

        String statementStr = "SELECT * FROM " + Constants.DB.RECURRING_EVENTS_META + ";";

        try (PreparedStatement statement = connection.prepareStatement(statementStr)) {
            ResultSet resultSet = statement.executeQuery();

            if (resultSet == null) throw new IllegalStateException("resultSet for createScheduledEventManager is null!");

            Map<String, String> guildToChannelRecurringEventMap = new HashMap<>();

            while (resultSet.next()) {
                String guild = resultSet.getString("guild");
                String textChannel = resultSet.getString("text_channel");


                guildToChannelRecurringEventMap.put(guild, textChannel);
            }

            manager.setGuildToChannelMap(guildToChannelRecurringEventMap);
            resultSet.close();

        } catch (SQLException e) {
            LOGGER.warn("Unable to execute ScheduledEventManager SQL statement! Reverting to no loads", e);
        }

        statementStr = "SELECT * FROM " + Constants.DB.RECURRING_EVENTS + ";";

        try (PreparedStatement statement = connection.prepareStatement(statementStr)) {
            ResultSet resultSet = statement.executeQuery();

            if (resultSet == null) throw new IllegalStateException("resultSet for createScheduledEventManager is null!");

            Map<String, RecurringEvent> eventIdToRecurringEvents = new HashMap<>();

            while (resultSet.next()) {
                String guild = resultSet.getString("guild");
                String event = resultSet.getString("event");
                String member = resultSet.getString("creator");
                String frequency = resultSet.getString("frequency");

                RecurringEvent recurringEvent = RecurringEvent.from(jda, guild, event, member, frequency);
                eventIdToRecurringEvents.put(event, recurringEvent);
            }

            manager.setEventIdToRecurringEvents(eventIdToRecurringEvents);
            resultSet.close();

        } catch (SQLException e) {
            LOGGER.warn("Unable to execute ScheduledEventManager SQL statement! Reverting to no loads", e);
        }
    }
}
