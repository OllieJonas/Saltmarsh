package me.olliejonas.saltmarsh;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.olliejonas.saltmarsh.music.SpotifyWrapped;
import me.olliejonas.saltmarsh.poll.PollManager;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Supplier;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    static final String NO_DB = "Unable to connect to database! This functionality has therefore been disabled!";

    public record Props(String discToken, HikariDataSource dataSource, SpotifyWrapped spotify, boolean developerMode) {

    }

    public static void main(String[] args) {
        // not null
        String discToken = getDiscordToken(args);
        Boolean developerMode = getEnvVariable("DEVELOPER_MODE", Boolean.class, false);

        // could be null
        String sqlUsername = getEnvVariable("MYSQL_USER", "root");  // these aren't the actual credentials lol
        String sqlPassword = getEnvVariable("MYSQL_PASSWORD", "hello");
        String sqlHost = getEnvVariable("MYSQL_HOST", "localhost");

        HikariDataSource dataSource = connectToDB(sqlUsername, sqlPassword, sqlHost);

        // sql
        try {
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                setupInitialDBTables(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String spotifyClient = getEnvVariable("SPOTIFY_CLIENT_ID", "");
        String spotifySecret = getEnvVariable("SPOTIFY_CLIENT_SECRET", "");

        SpotifyApi api = null;
        // spotify
        if (!spotifyClient.isEmpty() && !spotifySecret.isEmpty()) {
            api = SpotifyApi.builder()
                    .setClientId(spotifyClient)
                    .setClientSecret(spotifySecret)
                    .build();
        }

        SpotifyWrapped spotify = new SpotifyWrapped(api);

        Props props = new Props(discToken, dataSource, spotify, developerMode);

        Saltmarsh saltmarsh = new Saltmarsh(props);

        try {
            saltmarsh.init();
        } catch (LoginException exception) {
            LOGGER.error("Unable to init JDA!", exception);
        }
    }

    private static boolean setupInitialDBTables(Connection connection) throws SQLException {
        if (connection == null) return false;

        boolean createRE = setupRecurringEvents(connection);
        boolean createSE = setupScheduledEvents(connection);
        boolean setupPolls = setupPolls(connection);

        return createRE && createSE && setupPolls;
    }

    private static boolean setupRecurringEvents(Connection connection) throws SQLException {
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

        createREMStmt.close();
        createREStmt.close();

        return createREM && createRE;
    }

    private static boolean setupScheduledEvents(Connection connection) throws SQLException {

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

        createSEMStmt.close();
        createSEStmt.close();

        return createSEM && createSE;
    }

    private static boolean setupPolls(Connection connection) throws SQLException {
        String createPollsStmtStr =
                "CREATE TABLE IF NOT EXISTS " + Constants.DB.POLLS +
                        " (`message_id` VARCHAR(64), `question` VARCHAR(" + PollManager.QUESTION_MAX_LENGTH + "), " +
                        "`creator` VARCHAR(64), " +
                        "`anonymous` TINYINT, `singular` TINYINT, `text_repr` TINYINT, " +
                        "`options` VARCHAR (" + PollManager.SQL_OPTION_LENGTH + "), PRIMARY KEY(`message_id`)); ";

        String createPollOptionsStmtStr =
                "CREATE TABLE IF NOT EXISTS " + Constants.DB.POLL_OPTIONS +
                        " (`id` INT AUTO_INCREMENT, `message_id` VARCHAR(64), `guild` VARCHAR(64)," +
                        " `voter` VARCHAR(64), `option` SMALLINT UNSIGNED, PRIMARY KEY(`id`));";

        PreparedStatement createPollsStmt = connection.prepareStatement(createPollsStmtStr + createPollOptionsStmtStr);
        boolean createPolls = createPollsStmt.execute();

        createPollsStmt.close();

        return createPolls;
    }

    private static HikariDataSource connectToDB(String name, String password, String host) {
        LOGGER.info("Attempting to connect to MySQL database " +
                "(" + host + ") with username \"" + name + "\" and password \"" + password + "\" ...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":3306/saltmarsh?autoReconnect=true&allowMultiQueries=true");
        config.setUsername(name);
        config.setPassword(password);

        boolean validConnection;

        HikariDataSource hikariDataSource = null;

        try {
            hikariDataSource = new HikariDataSource(config);
            validConnection = hikariDataSource.getConnection().isValid(10000);
        } catch (Exception e) {
            LOGGER.warn(NO_DB);
            return null;
        }

        if (!validConnection)
            LOGGER.warn(NO_DB);

        return hikariDataSource;
    }

    private static String getDiscordToken(String[] args) {
        return args.length > 0 ? args[0] : getEnvVariable("SALTMARSH_DISCORD_TOKEN");
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
