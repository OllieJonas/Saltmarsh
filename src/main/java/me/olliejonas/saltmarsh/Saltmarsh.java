package me.olliejonas.saltmarsh;

import lombok.Getter;
import me.olliejonas.saltmarsh.command.admin.AdminCommand;
import me.olliejonas.saltmarsh.command.meta.commands.HelpCommand;
import me.olliejonas.saltmarsh.command.misc.ClearBotMessagesCommand;
import me.olliejonas.saltmarsh.command.misc.SayInAnEchoingVoiceCommand;
import me.olliejonas.saltmarsh.command.debug.TestCommand;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandListener;
import me.olliejonas.saltmarsh.command.meta.CommandRegistry;
import me.olliejonas.saltmarsh.command.meta.CommandWatchdog;
import me.olliejonas.saltmarsh.command.misc.HelloWorldCommand;
import me.olliejonas.saltmarsh.command.roll.RollCommand;
import me.olliejonas.saltmarsh.command.watchdog.WatchdogCommand;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.VoiceAFKTimeoutTaskScheduler;
import me.olliejonas.saltmarsh.music.commands.*;
import me.olliejonas.saltmarsh.poll.PollCommand;
import me.olliejonas.saltmarsh.poll.PollEmbedManager;
import me.olliejonas.saltmarsh.util.AutoEnableWatchdog;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbedListener;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class Saltmarsh {

    @Getter
    private final String jdaToken;

    private JDA jda;

    private final Set<ListenerAdapter> listeners;

    private final Collection<GatewayIntent> intents;

    private final ButtonEmbedManager buttonEmbedManager;

    private final PaginatedEmbedManager paginatedEmbedManager;

    private final PollEmbedManager pollEmbedManager;

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
        this.audioManager = new GlobalAudioManager();

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

        // other random stuff (should require jda)
        VoiceAFKTimeoutTaskScheduler voiceAFKTimeoutTaskScheduler = new VoiceAFKTimeoutTaskScheduler(this.jda);
        voiceAFKTimeoutTaskScheduler.run();

        AutoEnableWatchdog.autoEnable(jda, commandWatchdog);
    }

    public void destroy() {
        this.jda.shutdownNow();
    }

    public void registerListeners() {
        registerListener(new CommandListener(this.commandRegistry, this.commandWatchdog, Constants.COMMAND_PREFIXES));
        registerListener(new ButtonEmbedListener(this.buttonEmbedManager));
    }

    public void registerCommands() {

        // misc
        registerCommand(new HelloWorldCommand());
        registerCommand(new SayInAnEchoingVoiceCommand());
        registerCommand(new ClearBotMessagesCommand());
        registerCommand(new RollCommand());
        registerCommand(new WatchdogCommand(commandWatchdog));
        registerCommand(new PollCommand(pollEmbedManager));

        // admin stuff
        registerCommand(new TestCommand(
                this.buttonEmbedManager,
                this.paginatedEmbedManager,
                this.pollEmbedManager,
                this.audioManager
        ));

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
                .enableCache(CacheFlag.VOICE_STATE)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("Sea Shanties"))
                .build();
    }
}
