package me.olliejonas.saltmarsh;

import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventListener;
import me.olliejonas.saltmarsh.scheduledevents.ScheduledEventManager;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEvent;
import me.olliejonas.saltmarsh.scheduledevents.recurring.RecurringEventManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

// to stop me from typing a bunch of commands on EVERY startup
// only relevant whilst DB isn't setup
public class DevGuildSetupChannels extends ListenerAdapter {


    private static final String SALTMARSH_DEV_GUILD = "703739094059974786";

    private static final Map<String, Map<String, String>> SALTMARSH_CHANNEL_CONSTS = Map.of(
            SALTMARSH_DEV_GUILD, Map.of(
                    "general",  "703739094059974790",
                    "event_notifications_channel", "1163893719670456350",
                    "notification_role_id", "1161337031243350067",
                    "recurring_event_channel", "1163893760787226704"
    ));
    private final boolean enabled;

    private final ScheduledEventManager scheduledEventManager;

    private final RecurringEventManager recurringEventManager;

    private final ScheduledEventListener scheduledEventListener;

    public DevGuildSetupChannels(boolean enabled,
                                 ScheduledEventManager scheduledEventManager,
                                 RecurringEventManager recurringEventManager,
                                 ScheduledEventListener scheduledEventListener) {
        this.enabled = enabled;
        this.scheduledEventManager = scheduledEventManager;
        this.recurringEventManager = recurringEventManager;
        this.scheduledEventListener = scheduledEventListener;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (event.getGuild().getId().equals(SALTMARSH_DEV_GUILD))
            register(event.getGuild(), false);
    }

    private void register(Guild guild, boolean refreshEvents) {
        if (!SALTMARSH_CHANNEL_CONSTS.containsKey(guild.getId())) return;

        Map<String, String> consts = SALTMARSH_CHANNEL_CONSTS.get(guild.getId());
        scheduledEventManager.addNotificationChannel(guild,
                Objects.requireNonNull(guild.getTextChannelById(consts.get("event_notifications_channel"))));
        scheduledEventManager.addRole(guild,
                Objects.requireNonNull(guild.getRoleById(consts.get("notification_role_id"))));
        recurringEventManager.addChannel(guild, Objects.requireNonNull(guild.getTextChannelById(consts.get("recurring_event_channel"))));

        if (refreshEvents) {
            guild.getScheduledEvents().forEach(ev -> ev.delete().queue());

            OffsetDateTime start = OffsetDateTime.now().plus(2, ChronoUnit.WEEKS);
            guild.createScheduledEvent("wee woo", "my house",
                    start, start.plus(2, ChronoUnit.HOURS)).setDescription("fun for the whole family!")
                    .queueAfter(3, TimeUnit.SECONDS,
                            success -> recurringEventManager.registerEventAsRecurring(success,
                                    RecurringEvent.of(success, RecurringEvent.Frequency.WEEKLY), guild, scheduledEventListener));
        }
    }
}
