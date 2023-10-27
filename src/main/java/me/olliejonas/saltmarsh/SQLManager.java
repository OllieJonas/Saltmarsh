package me.olliejonas.saltmarsh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public abstract class SQLManager {

    static final Logger LOGGER = LoggerFactory.getLogger(SQLManager.class);

    private Connection connection;

    public SQLManager(Connection connection) {
        this.connection = connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected Optional<ResultSet> executeQuery(String sql) {
        if (connection == null) return Optional.empty();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return Optional.of(statement.executeQuery());
        } catch (SQLException e) {
            LOGGER.error("Unable to execute query " + sql + "!", e);
            return Optional.empty();
        }
    }

    protected boolean execute(String sql) {
        if (connection == null) return false;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Unable to execute query " + sql + "!", e);
            return false;
        }
    }

    protected Optional<PreparedStatement> prepareStatement(String sql) {
        if (connection == null) return Optional.empty();

        try {
            return Optional.of(connection.prepareStatement(sql));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
}
