package me.olliejonas.saltmarsh;

import lombok.Getter;
import me.olliejonas.saltmarsh.command.admin.AdminCommand;
import me.olliejonas.saltmarsh.command.debug.TestCommand;
import me.olliejonas.saltmarsh.command.debug.WhatTypeIsCommand;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandListener;
import me.olliejonas.saltmarsh.command.meta.CommandRegistry;
import me.olliejonas.saltmarsh.command.meta.CommandWatchdog;
import me.olliejonas.saltmarsh.command.meta.commands.HelpCommand;
import me.olliejonas.saltmarsh.command.misc.ClearBotMessagesCommand;
import me.olliejonas.saltmarsh.command.misc.HelloWorldCommand;
import me.olliejonas.saltmarsh.command.misc.IsThisAURLCommand;
import me.olliejonas.saltmarsh.command.misc.SayInAnEchoingVoiceCommand;
import me.olliejonas.saltmarsh.command.roll.RollCommand;
import me.olliejonas.saltmarsh.command.roll.ValidateIntegrityCommand;
import me.olliejonas.saltmarsh.command.watchdog.WatchdogCommand;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedListener;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import me.olliejonas.saltmarsh.embed.input.InputEmbedListener;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.commands.*;
import me.olliejonas.saltmarsh.poll.PollCommand;
import me.olliejonas.saltmarsh.poll.PollEmbedManager;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventListener;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import me.olliejonas.saltmarsh.scheduledevents.commands.GetEventPingStatusCommand;
import me.olliejonas.saltmarsh.scheduledevents.commands.ToggleEventPingCommand;
import me.olliejonas.saltmarsh.scheduledevents.commands.ToggleEventPingRolesCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.*;

public class Saltmarsh {

    @Getter
    private final String jdaToken;

    private JDA jda;

    private final Set<ListenerAdapter> listeners;

    private final Collection<GatewayIntent> intents;

    private final ButtonEmbedManager buttonEmbedManager;

    private final PaginatedEmbedManager paginatedEmbedManager;

    private final PollEmbedManager pollEmbedManager;

    private final InputEmbedManager inputEmbedManager;

    private final ScheduledEventManager scheduledEventManager;

    private final GlobalAudioManager audioManager;

    private final CommandRegistry commandRegistry;

    private final CommandWatchdog commandWatchdog;

    public Saltmarsh(String jdaToken) {
        this.jdaToken = jdaToken;
        this.listeners = new HashSet<>();
        this.intents = new HashSet<>();

        // managers
        this.buttonEmbedManager = new ButtonEmbedManager();
        this.paginatedEmbedManager = new PaginatedEmbedManager(buttonEmbedManager);
        this.pollEmbedManager = new PollEmbedManager(buttonEmbedManager);
        this.inputEmbedManager = new InputEmbedManager(buttonEmbedManager);
        this.audioManager = new GlobalAudioManager();
        this.scheduledEventManager = new ScheduledEventManager();

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

        // register HelloWorld as a global command to get the "Supports Slash Commands" badge
        Command command = new HelloWorldCommand();
        SlashCommandData helloWorldData = Commands.slash(command.getPrimaryAlias(), command.info().shortDesc());
        this.jda.updateCommands().addCommands(helloWorldData).queue();

//        AutoEnableWatchdog.autoEnable(jda, commandWatchdog);
    }

    public void destroy() {
        this.jda.shutdownNow();
    }

    public void registerListeners() {
        registerListener(new CommandListener(this.commandRegistry, this.commandWatchdog));
        registerListener(new ButtonEmbedListener(this.buttonEmbedManager));
        registerListener(new InputEmbedListener(this.inputEmbedManager));
        registerListener(new ScheduledEventListener(this.scheduledEventManager));
    }

    public void registerCommands() {

        // events
        registerCommand(new ToggleEventPingCommand(this.scheduledEventManager));
        registerCommand(new ToggleEventPingRolesCommand(this.scheduledEventManager));
        registerCommand(new GetEventPingStatusCommand(this.scheduledEventManager));

        // misc
        registerCommand(new IsThisAURLCommand());
        registerCommand(new SayInAnEchoingVoiceCommand());
        registerCommand(new ClearBotMessagesCommand());
        registerCommand(new RollCommand());
        registerCommand(new ValidateIntegrityCommand(new Random()));
        registerCommand(new WatchdogCommand(commandWatchdog));

        // poll
        registerCommand(new PollCommand(pollEmbedManager, inputEmbedManager));

        // admin stuff
        registerCommand(new TestCommand(
                this.buttonEmbedManager,
                this.paginatedEmbedManager,
                this.pollEmbedManager,
                this.inputEmbedManager,
                this.audioManager
        ));
        registerCommand(new WhatTypeIsCommand());

        registerCommand(new AdminCommand(
                this.audioManager
        ));

        // music commands
        registerCommand(new DisconnectCommand(this.audioManager));
        registerCommand(new PauseCommand(this.audioManager));
        registerCommand(new PlayCommand(this.audioManager));
        registerCommand(new RepeatingCommand(this.audioManager));
        registerCommand(new ResumeCommand(this.audioManager));
        registerCommand(new ShuffleCommand(this.audioManager));
        registerCommand(new SkipCommand(this.audioManager));
        registerCommand(new StopCommand(this.audioManager));
        registerCommand(new QueueCommand(this.audioManager, this.paginatedEmbedManager));

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



    public JDA buildJda() {
        return JDABuilder.createLight(this.jdaToken,
                        EnumSet.allOf(GatewayIntent.class) // ignoring the whole registerIntents thing because cba
                )
                .addEventListeners(listeners.toArray(new Object[0]))
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setEventPassthrough(true)
                .enableCache(CacheFlag.VOICE_STATE, CacheFlag.SCHEDULED_EVENTS)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("Sea Shanties \uD83C\uDFB5"))
                .build();
    }
}
