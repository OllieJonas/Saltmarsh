package me.olliejonas.saltmarsh;

import lombok.Getter;
import me.olliejonas.saltmarsh.util.structures.WeightedRandomSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PresenceUpdaterTask {

    static final WeightedRandomSet<Activity> ACTIVITIES = new WeightedRandomSet<>();

    static {
        ACTIVITIES.add(Activity.listening("Sea Shanties \uD83C\uDFB5"), 0.998);
        ACTIVITIES.add(Activity.customStatus("help me im locked in a basement"), 0.01);
        ACTIVITIES.add(Activity.listening("your mum in bed"), 0.01);
    }

    static final long DEFAULT_TIME = 5;

    static final TimeUnit DEFAULT_UNIT = TimeUnit.MINUTES;

    private final JDA jda;

    private final WeightedRandomSet<Activity> activities;

    private final long time;

    private final TimeUnit unit;

    @Getter
    private final ScheduledExecutorService executorService;

    private Activity currentActivity;

    public PresenceUpdaterTask(JDA jda) {
        this(jda, ACTIVITIES, DEFAULT_TIME, DEFAULT_UNIT);
    }
    public PresenceUpdaterTask(JDA jda, WeightedRandomSet<Activity> activities, long time, TimeUnit unit) {
        this.jda = jda;
        this.activities = activities;
        this.time = time;
        this.unit = unit;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void run() {
        executorService.scheduleAtFixedRate(this::chooseNext, 0, time, unit);
    }

    private void chooseNext() {
        Activity activity = activities.getRandom();

        if (currentActivity == null || activity != currentActivity)
            jda.getPresence().setActivity(activity);

        this.currentActivity = activity;
    }
}
