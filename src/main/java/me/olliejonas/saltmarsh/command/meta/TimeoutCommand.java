package me.olliejonas.saltmarsh.command.meta;

import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Member;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class TimeoutCommand extends Command {

    static final long CLEANER_RATE = 30;

    static final TimeUnit CLEANER_RATE_UNIT = TimeUnit.MINUTES;

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    private final Map<Member, Long> lastExecutedMap;


    public TimeoutCommand(String alias) {
        this(CommandPermissions.ALL, alias, Collections.emptySet(), new HashMap<>());
    }

    public TimeoutCommand(String primaryAlias, String... aliases) {
        this(CommandPermissions.ALL, primaryAlias, Set.of(aliases), new HashMap<>());
    }

    public TimeoutCommand(CommandPermissions permissions, String primaryAlias) {
        this(permissions, primaryAlias, Collections.emptySet(), new HashMap<>());
    }

    public TimeoutCommand(CommandPermissions permissions, String primaryAlias, String... aliases) {
        this(permissions, primaryAlias, Set.of(aliases), new HashMap<>());
    }

    public TimeoutCommand(CommandPermissions permissions, String primaryAlias, Set<String> aliases) {
        this(permissions, primaryAlias, aliases, new HashMap<>());
    }

    public TimeoutCommand(CommandPermissions permissions, String primaryAlias, Set<String> aliases, Map<String, Command> subCommands) {
        super(permissions, primaryAlias, aliases, subCommands);

        this.lastExecutedMap = new HashMap<>();
        executorService.scheduleAtFixedRate(new Cleaner(), 0, CLEANER_RATE, CLEANER_RATE_UNIT);
    }

    public abstract long timeout();

    public abstract TimeUnit timeoutUnit();

    protected InteractionResponses timedOut(Member member) {
        boolean isTimedOut = isTimedOut(member);
        if (!isTimedOut)
            lastExecutedMap.put(member, System.currentTimeMillis());

        return isTimedOut ? InteractionResponses.error("You need to wait at least " + timeout() + " " + timeoutUnit().name() + " before using this command again!") : null;
    }

    private boolean isTimedOut(Member member) {
        if (!lastExecutedMap.containsKey(member) || CommandPermissions.ADMIN.hasPermission(member))
            return false;

        return timeoutUnit().toMillis(lastExecutedMap.get(member) + timeout()) <= System.currentTimeMillis();
    }

    class Cleaner implements Runnable {

        @Override
        public void run() {
            Objects.requireNonNull(timeoutUnit());

            lastExecutedMap.keySet().stream()
                    .filter(member -> !isTimedOut(member))
                    .forEach(lastExecutedMap::remove);
        }
    }
}
