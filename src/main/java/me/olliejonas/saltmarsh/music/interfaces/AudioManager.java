package me.olliejonas.saltmarsh.music.interfaces;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface AudioManager {

    // returns response message (either error or successfully played)
    String playTrack(Member executor, String identifier);


    GuildAudioManager getGuildManager(Guild guild);

    default boolean pause(Guild guild) {
        return getGuildManager(guild).togglePause();
    }

    default boolean disconnect(Guild guild) {
        return getGuildManager(guild).disconnect();
    }

    default void sendNowPlayingPrompt(Guild guild, TextChannel channel) {
        getGuildManager(guild).sendNowPlayingPrompt(channel);
    }
    default AudioTrack skip(Guild guild, Integer skip) {
        return getGuildManager(guild).skip(skip);
    }

    default boolean stop(Guild guild) {
        return getGuildManager(guild).stop();
    }

    default boolean shuffle(Guild guild) {
        return getGuildManager(guild).shuffle();
    }
}
