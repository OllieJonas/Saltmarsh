package me.olliejonas.saltmarsh;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.olliejonas.saltmarsh.command.debug.AmIConnectedToADatabaseCommand;
import me.olliejonas.saltmarsh.command.debug.TestCommand;
import me.olliejonas.saltmarsh.command.debug.WhatTypeIsCommand;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandListener;
import me.olliejonas.saltmarsh.command.meta.CommandRegistry;
import me.olliejonas.saltmarsh.command.meta.CommandWatchdog;
import me.olliejonas.saltmarsh.command.meta.commands.HelpCommand;
import me.olliejonas.saltmarsh.command.misc.ClearBotMessagesCommand;
import me.olliejonas.saltmarsh.command.misc.HelloWorldCommand;
import me.olliejonas.saltmarsh.command.misc.SayInAnEchoingVoiceCommand;
import me.olliejonas.saltmarsh.command.roll.RollCommand;
import me.olliejonas.saltmarsh.command.roll.ValidateIntegrityCommand;
import me.olliejonas.saltmarsh.command.watchdog.WatchdogCommand;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedListener;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManagerImpl;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManagerImpl;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedListener;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManagerImpl;
import me.olliejonas.saltmarsh.music.AudioManagerImpl;
import me.olliejonas.saltmarsh.music.commands.*;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import me.olliejonas.saltmarsh.poll.PollCommand;
import me.olliejonas.saltmarsh.poll.PollLoader;
import me.olliejonas.saltmarsh.poll.PollManager;
import me.olliejonas.saltmarsh.poll.PollManagerImpl;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventListener;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManagerImpl;
import me.olliejonas.saltmarsh.scheduledevents.commands.GetEventPingStatusCommand;
import me.olliejonas.saltmarsh.scheduledevents.commands.ToggleEventPingCommand;
import me.olliejonas.saltmarsh.scheduledevents.commands.ToggleEventPingRolesCommand;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventListener;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventLoader;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManagerImpl;
import me.olliejonas.saltmarsh.scheduledevents.recurring.commands.DeleteRecurringEventCommand;
import me.olliejonas.saltmarsh.scheduledevents.recurring.commands.EditRecurringEventCommand;
import me.olliejonas.saltmarsh.scheduledevents.recurring.commands.RecurEventCommand;
import me.olliejonas.saltmarsh.scheduledevents.recurring.commands.RegisterRecurringChannelCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Saltmarsh {

    static final Logger LOGGER = LoggerFactory.getLogger(Saltmarsh.class);

    @Getter
    private final String jdaToken;

    private JDA jda;

    private final HikariDataSource hikariDataSource;

    @Getter
    private Connection sqlConnection = null;

    @Getter
    private final boolean databaseEnabled;

    private final boolean developerMode;

    private final Set<ListenerAdapter> listeners;

    private final Collection<GatewayIntent> intents;

    // --------------- managers ---------------
    private final AudioManager audioManager;

    private final PollManager pollManager;

    private final RecurringEventManager recurringEventManager;

    private final ScheduledEventManager scheduledEventManager;

    private final ButtonEmbedManager buttonEmbedManager;

    private final PaginatedEmbedManager paginatedEmbedManager;

    private final WizardEmbedManager wizardEmbedManager;

    private PresenceUpdaterTask presenceUpdaterTask;


    // --------------- listeners (that need to be accessible) ---------------

    private final ScheduledEventListener scheduledEventListener;

    private final CommandRegistry commandRegistry;

    private final CommandWatchdog commandWatchdog;

    public Saltmarsh(String jdaToken, HikariDataSource database, boolean developerMode) {
        this.jdaToken = jdaToken;
        this.hikariDataSource = database;
        this.developerMode = developerMode;

        if (hikariDataSource != null) {
            try {
                this.sqlConnection = database.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        this.databaseEnabled = hikariDataSource != null;

        this.listeners = new HashSet<>();
        this.intents = new HashSet<>();

        // managers
        this.buttonEmbedManager = new ButtonEmbedManagerImpl();
        this.paginatedEmbedManager = new PaginatedEmbedManagerImpl(buttonEmbedManager);
        this.wizardEmbedManager = new WizardEmbedManagerImpl();

        this.audioManager = createAudioManager();

        this.pollManager = new PollManagerImpl(sqlConnection, buttonEmbedManager);
        registerListener(new PollLoader(pollManager, sqlConnection));

        // RecurringEventManager requires JDA to be loaded, so loading is handled in RecurringEventLoader
        this.recurringEventManager = new RecurringEventManagerImpl(wizardEmbedManager);
        registerListener(new RecurringEventLoader(recurringEventManager, sqlConnection));

        this.scheduledEventManager = createScheduledEventManager();

        // listeners
        this.scheduledEventListener = new ScheduledEventListener(this.scheduledEventManager,
                this.recurringEventManager);

        ((RecurringEventManagerImpl) this.recurringEventManager).setScheduledEventListener(scheduledEventListener);

        // commands
        this.commandRegistry = new CommandRegistry();
        this.commandWatchdog = new CommandWatchdog(buttonEmbedManager);
    }

    public void init() throws LoginException {
        // don't change this order please ok thanks
        registerCommands();
        registerListeners();
        registerIntents();

        try {
            this.jda = buildJda().awaitReady();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        // register HelloWorld as a global command (doesn't work) to get the "Supports Slash Commands" badge
        Command command = new HelloWorldCommand();
        SlashCommandData helloWorldData = Commands.slash(command.getPrimaryAlias(), command.info().shortDesc());
        this.jda.updateCommands().addCommands(helloWorldData).queue();

        this.presenceUpdaterTask = new PresenceUpdaterTask(jda);
        presenceUpdaterTask.run();

        // shutdown hook to gracefully terminate program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                destroy();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public void destroy() throws SQLException {
        LOGGER.info("Shutting down ...");
        this.jda.shutdownNow();
        this.presenceUpdaterTask.getExecutorService().shutdownNow();

        if (sqlConnection != null)
            this.sqlConnection.close();

        if (hikariDataSource != null)
            this.hikariDataSource.close();
    }

    public void registerListeners() {
        registerListener(new CommandListener(this.commandRegistry, this.commandWatchdog));
        registerListener(new ButtonEmbedListener(this.buttonEmbedManager));
        registerListener(new WizardEmbedListener(this.wizardEmbedManager));
        registerListener(this.scheduledEventListener);
        registerListener(new RecurringEventListener(this.recurringEventManager));
    }

    public void registerCommands() {
        // music
        registerCommand(new DisconnectCommand(this.audioManager));
        registerCommand(new NowPlayingCommand(this.audioManager));
        registerCommand(new PlayCommand(this.audioManager));
        registerCommand(new StopCommand(this.audioManager));
        registerCommand(new QueueCommand(this.paginatedEmbedManager, this.audioManager));
        registerCommand(new SkipCommand(this.audioManager));

        // events
        registerCommand(new ToggleEventPingCommand(this.scheduledEventManager));
        registerCommand(new ToggleEventPingRolesCommand(this.scheduledEventManager));
        registerCommand(new GetEventPingStatusCommand(this.scheduledEventManager));

        registerCommand(new RegisterRecurringChannelCommand(this.recurringEventManager));
        registerCommand(new RecurEventCommand(this.wizardEmbedManager, this.recurringEventManager));
        registerCommand(new DeleteRecurringEventCommand(this.wizardEmbedManager, this.recurringEventManager));
        registerCommand(new EditRecurringEventCommand(this.wizardEmbedManager, this.recurringEventManager));

        // misc
        registerCommand(new AmIConnectedToADatabaseCommand(databaseEnabled));
        registerCommand(new SayInAnEchoingVoiceCommand());
        registerCommand(new ClearBotMessagesCommand());
        registerCommand(new RollCommand());
        registerCommand(new ValidateIntegrityCommand(new Random()));
        registerCommand(new WatchdogCommand(commandWatchdog));

        // poll
        registerCommand(new PollCommand(pollManager, wizardEmbedManager));

        // admin stuff
        registerCommand(new TestCommand(!this.developerMode,
                this.audioManager,
                this.buttonEmbedManager,
                this.paginatedEmbedManager,
                this.pollManager,
                this.wizardEmbedManager
        ));

        registerCommand(new WhatTypeIsCommand());

        // help - register this last
        registerCommand(new HelpCommand(paginatedEmbedManager, commandRegistry));
    }

    public void registerIntents() {
        registerIntent(GatewayIntent.MESSAGE_CONTENT);
        registerIntent(GatewayIntent.GUILD_MEMBERS);
        registerIntent(GatewayIntent.GUILD_MESSAGES);
        registerIntent(GatewayIntent.GUILD_MESSAGE_REACTIONS);
    }

    private void registerListener(ListenerAdapter listener) {
        listeners.add(listener);
    }

    private void registerCommand(Command command) {
        this.commandRegistry.register(command);
    }

    private void registerIntent(GatewayIntent intent) {
        intents.add(intent);
    }

    private AudioManager createAudioManager() {
        AudioPlayerManager manager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(manager);
        AudioSourceManagers.registerLocalSource(manager);

        return new AudioManagerImpl(manager);
    }
    private ScheduledEventManager createScheduledEventManager() {
        if (sqlConnection == null) return new ScheduledEventManagerImpl(this.recurringEventManager);

        Map<String, String> guildToPingChannelMap = new ConcurrentHashMap<>();
        Map<String, String> guildToRoleMap = new ConcurrentHashMap<>();
        Map<String, String> eventToCreatorMap = new ConcurrentHashMap<>();

        Map<String, Map<String, Tuple2<String, String>>> eventToChannelToMessageMap = new ConcurrentHashMap<>();

        try (PreparedStatement statement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB.SCHEDULED_EVENTS_META + ";")) {
            ResultSet resultSet = statement.executeQuery();

            if (resultSet == null) throw new IllegalStateException("resultSet for createScheduledEventManager is null!");

            while (resultSet.next()) {
                String guild = resultSet.getString("guild");
                String channel = resultSet.getString("channel");
                String role = resultSet.getString("role");

                Objects.requireNonNull(guild);
                Objects.requireNonNull(channel);
                Objects.requireNonNull(role);

                guildToPingChannelMap.put(guild, channel);
                guildToRoleMap.put(guild, role);
            }

            resultSet.close();

        } catch (SQLException e) {
            LOGGER.warn("Unable to execute ScheduledEventManager SQL statement! Reverting to no loads", e);
            return new ScheduledEventManagerImpl(recurringEventManager);
        }

        try (PreparedStatement statement = sqlConnection.prepareStatement("SELECT * FROM " + Constants.DB.SCHEDULED_EVENTS + ";")) {
            ResultSet resultSet = statement.executeQuery();

            if (resultSet == null) throw new IllegalStateException("resultSet for createScheduledEventManager is null!");

            while (resultSet.next()) {
                String event = resultSet.getString("event");
                String channel = resultSet.getString("channel");
                String creator = resultSet.getString("creator");
                String embedMessage = resultSet.getString("embed_message");
                String pingMessage = resultSet.getString("ping_message");

                eventToCreatorMap.put(event, creator);

                Map<String, Tuple2<String, String>> channelToMessageMap = new HashMap<>();

                channelToMessageMap.put(channel, new Tuple2<>(embedMessage, pingMessage));
                eventToChannelToMessageMap.put(event, channelToMessageMap);
            }
            resultSet.close();

        } catch (SQLException e) {
            LOGGER.warn("Unable to execute ScheduledEventManager SQL statement! Reverting to no loads", e);
            return new ScheduledEventManagerImpl(recurringEventManager);
        }

        return new ScheduledEventManagerImpl(sqlConnection,
                this.recurringEventManager,
                guildToPingChannelMap,
                eventToChannelToMessageMap,
                guildToRoleMap,
                eventToCreatorMap);
    }



    public JDA buildJda() {
        return JDABuilder.createLight(this.jdaToken,
                        EnumSet.allOf(GatewayIntent.class) // ignoring the whole registerIntents thing because cba
                )
                .addEventListeners(listeners.toArray(new Object[0]))
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setEventPassthrough(true)
                .enableCache(CacheFlag.VOICE_STATE, CacheFlag.SCHEDULED_EVENTS)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();
    }
}
