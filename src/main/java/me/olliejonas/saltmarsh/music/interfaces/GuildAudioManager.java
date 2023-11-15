package me.olliejonas.saltmarsh.music.interfaces;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.music.structures.AudioQueue;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface GuildAudioManager {

    String connect(Member member);

    int clearQueue();

    boolean disconnect();

    AudioSendHandler getSendHandler();

    AudioLoadResultHandler getTrackLoader(SlashCommandInteractionEvent event);

    AudioQueue<AudioTrack> getTracks();

    AudioTrack playNext();

    boolean stop();

    boolean togglePause();

    void sendNowPlayingPrompt(TextChannel channel);

    boolean shuffle();

    AudioTrack skip(Integer skip);
}
