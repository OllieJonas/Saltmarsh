package me.olliejonas.saltmarsh.music.interfaces;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

public interface AudioManager {

    // returns response message (either error or successfully played)
    String addTrack(SlashCommandInteractionEvent event, Member executor, String identifier) throws IOException, ParseException, SpotifyWebApiException;


    default int clearQueue(Guild guild) {
        return getGuildManager(guild).clearQueue();
    }

    GuildAudioManager getGuildManager(Guild guild);

    default boolean disconnect(Guild guild) {
        return getGuildManager(guild).disconnect();
    }

    default boolean pause(Guild guild) {
        return getGuildManager(guild).togglePause();
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
