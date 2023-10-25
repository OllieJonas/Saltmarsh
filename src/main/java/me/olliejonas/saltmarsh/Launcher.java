package me.olliejonas.saltmarsh;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Supplier;

public class Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    static final String NO_DB = "Unable to connect to database! This functionality has therefore been disabled!";
    public static void main(String[] args) {
        // not null
        String discToken = getDiscordToken(args);

        // could be null
        String sqlUsername = getEnvVariable("MYSQL_USERNAME", "root");
        String sqlPassword = getEnvVariable("MYSQL_PASSWORD", "hello");


        HikariDataSource dataSource = connectToDB(sqlUsername, sqlPassword);

        try {
            setupInitialDBTables(dataSource.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Saltmarsh saltmarsh = new Saltmarsh(discToken, dataSource);

        try {
            saltmarsh.init();
        } catch (LoginException exception) {
            LOGGER.error("Unable to init JDA!", exception);
        }
    }

    private static boolean setupInitialDBTables(Connection connection) throws SQLException {
        if (connection == null) return false;

        String createDbStmtStr = "CREATE DATABASE IF NOT EXISTS " + Constants.APP_TITLE + " CHARACTER SET UTF8MB4;";
        PreparedStatement createDbStmt = connection.prepareStatement(createDbStmtStr);
        boolean createDb = createDbStmt.execute();

        // recurring events
        String createREMStmtStr =
                "CREATE TABLE IF NOT EXISTS " + Constants.DB.RECURRING_EVENTS_META +
                "(`guild` VARCHAR(64) NOT NULL, `text_channel` VARCHAR(64), PRIMARY KEY(`guild`)); ";
                        
        String createREStmtStr = "CREATE TABLE IF NOT EXISTS " + Constants.DB.RECURRING_EVENTS +
                "(`event` VARCHAR(64), `guild` VARCHAR(64), `creator` VARCHAR (64), " +
                "`frequency` VARCHAR(64), PRIMARY KEY(`event`));";

        PreparedStatement createREMStmt = connection.prepareStatement(createREMStmtStr);
        boolean createREM = createREMStmt.execute();

        PreparedStatement createREStmt = connection.prepareStatement(createREStmtStr);
        boolean createRE = createREStmt.execute();


        String createSEMStmtStr =
                "CREATE TABLE IF NOT EXISTS " + Constants.DB.SCHEDULED_EVENTS_META +
                "(`guild` VARCHAR(64) NOT NULL, `channel` VARCHAR(64), `role` VARCHAR(64), PRIMARY KEY(`guild`));";

        String createSEStmtStr =
                "CREATE TABLE IF NOT EXISTS " + Constants.DB.SCHEDULED_EVENTS +
                "(`event` VARCHAR(64), `channel` VARCHAR(64), `creator` VARCHAR(64), " +
                        "`embed_message` VARCHAR(64), " +
                        "`ping_message` VARCHAR(64), " +
                        "PRIMARY KEY(`event`));";

        PreparedStatement createSEMStmt = connection.prepareStatement(createSEMStmtStr);
        boolean createSEM = createSEMStmt.execute();

        PreparedStatement createSEStmt = connection.prepareStatement(createSEStmtStr);
        boolean createSE = createSEStmt.execute();

        createDbStmt.close();

        createREMStmt.close();
        createREStmt.close();

        createSEMStmt.close();
        createSEStmt.close();

        return createDb && (createRE && createREM) && (createSE && createSEM);
    }

    private static HikariDataSource connectToDB(String name, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/saltmarsh");
        config.setUsername(name);
        config.setPassword(password);

        HikariDataSource hikariDataSource = new HikariDataSource(config);
        boolean validConnection = false;

        try {
            validConnection = hikariDataSource.getConnection().isValid(10000);
        } catch (SQLException e) {
            LOGGER.warn(NO_DB);
            return null;
        }

        if (!validConnection)
            LOGGER.warn(NO_DB);

        return validConnection ? hikariDataSource : null;
    }

    private static String getDiscordToken(String[] args) {
        return args.length > 0 ? args[0] : getEnvVariable("SALTMARSH_DISCORD_TOKEN", true);
    }

    private static String getEnvVariable(String name) {
        return getEnvVariable(name, true);
    }

    private static String getEnvVariable(String name, String defaultValue) {
        return getEnvVariable(name, String.class, defaultValue);
    }

    private static <T> T getEnvVariable(String name, Class<T> clazz, T defaultValue) {
        String arg = System.getenv(name);

        if (arg == null || arg.isEmpty())
            return defaultValue;

        T converted = null;

        try {
            converted = StringToTypeConverter.cast(arg, clazz)
                    .orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException("value cannot be casted!"));
        } catch (Throwable e) {
            LOGGER.error("can't cast!", e);
        }

        return converted;
    }

    private static String getEnvVariable(String name, boolean exitIfNotFound) {
        String arg = System.getenv(name);

        if ((arg == null || arg.isEmpty()) && exitIfNotFound)
            throw new IllegalArgumentException(name + " is not allowed to be null!");

        return arg;
    }
}
