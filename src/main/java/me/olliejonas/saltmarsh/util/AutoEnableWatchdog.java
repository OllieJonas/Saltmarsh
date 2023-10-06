package me.olliejonas.saltmarsh.util;

import lombok.experimental.UtilityClass;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.command.meta.CommandWatchdog;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

@UtilityClass
public class AutoEnableWatchdog {

    private static final String MEMESK9_GUILD_ID = "777919186785533963";
    private static final String MEMESK9_WATCHDOG_CHANNEL_ID = "1075733851424436234";

    private static final String TEST_GUILD_ID = "703739094059974786";
    private static final String TEST_WATCHDOG_CHANNEL_ID = "1076196739788324944";

    private static final List<String> DEFAULT_TEXT_CHANNEL_IDS = List.of(
//            MEMESK9_WATCHDOG_CHANNEL_ID,
            TEST_WATCHDOG_CHANNEL_ID
    );

    public void autoEnable(JDA jda, CommandWatchdog watchdog) {

        int val = DEFAULT_TEXT_CHANNEL_IDS.stream()
                .reduce(0, (integer, s) -> enable(jda, watchdog, s) ? 1 : 0, Integer::sum);
    }

    private Boolean enable(JDA jda, CommandWatchdog watchdog, String textChannelId) {
        TextChannel channel = jda.getTextChannelById(textChannelId);

        if (channel != null) {
            watchdog.allocateChannel(channel.getGuild(), channel);
            channel.sendMessageEmbeds(
                    EmbedUtils.from("Saltmarsh has been enabled for this guild!"),
                    EmbedUtils.from(Constants.WATCHDOG_PREFIX + "Watchdog has been automatically enabled for this " +
                            "channel on startup!")).queue();
        }

        return channel != null;
    }
}
