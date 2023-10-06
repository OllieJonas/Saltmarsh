package me.olliejonas.saltmarsh.music;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class VoiceAFKTimeoutTaskScheduler {

    public static final long DEFAULT_DELETION_THRESHOLD = 120;

    public static final long DEFAULT_PERIOD = 60;

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;


    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceAFKTimeoutTaskScheduler.class);

    private final JDA jda;

    private final long period;

    private final TimeUnit timeUnit;

    private final ScheduledExecutorService service;

    private final Map<Long, Guild> marked;

    private final long deletionThresholdMillis;

    public VoiceAFKTimeoutTaskScheduler(JDA jda) {
        this(jda, DEFAULT_PERIOD, DEFAULT_DELETION_THRESHOLD, DEFAULT_TIME_UNIT);
    }

    public VoiceAFKTimeoutTaskScheduler(JDA jda, long deletionThresholdMillis) {
        this(jda, DEFAULT_PERIOD, deletionThresholdMillis, DEFAULT_TIME_UNIT);
    }

    public VoiceAFKTimeoutTaskScheduler(JDA jda, long period, long deletionThreshold, TimeUnit timeUnit) {
        this.jda = jda;
        this.period = period;
        this.timeUnit = timeUnit;
        this.deletionThresholdMillis = timeUnit.toMillis(deletionThreshold);

        this.service = Executors.newSingleThreadScheduledExecutor();
        this.marked = new HashMap<>();
    }

    public void run() {
        this.service.scheduleAtFixedRate(() -> {
            sweep();
            int deleted = delete();
        }, period, period, timeUnit);
    }

    public void sweep() {
        jda.getGuilds().stream()
                .filter(guild -> guild.getAudioManager().getConnectedChannel() != null) // filter out those where the bot is active
                .filter(guild -> guild.getAudioManager().getConnectedChannel().asVoiceChannel().getMembers().size() == 1) // only bot is connected
                .filter(guild -> guild.getAudioManager().getConnectedChannel().asVoiceChannel().getMembers().stream().allMatch(member -> member.getUser().isBot()))
                .forEach(guild -> marked.put(System.currentTimeMillis(), guild));
    }

    public int delete() {
        Predicate<Long> shouldRemovePredicate = start -> (System.currentTimeMillis() - start) >= deletionThresholdMillis;
        AtomicInteger deletedCount = new AtomicInteger(0);
        Set<Long> guilds = ConcurrentHashMap.newKeySet();

        marked.entrySet().stream().filter(e -> shouldRemovePredicate.test(e.getKey())).forEach(e -> {
            System.out.println("removing for afk!");
            e.getValue().getAudioManager().closeAudioConnection();
            deletedCount.incrementAndGet();
            guilds.add(e.getKey());
        });

        guilds.forEach(marked::remove);

        return deletedCount.get();
    }


}
