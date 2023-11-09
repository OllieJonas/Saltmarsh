package me.olliejonas.saltmarsh.music.interfaces;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.music.structures.AudioQueue;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface GuildAudioManager {

    String connect(Member member);

    boolean disconnect();

    AudioSendHandler getSendHandler();

    AudioLoadResultHandler getTrackLoader();

    AudioQueue<AudioTrack> getTracks();

    AudioTrack playNext();

    boolean stop();

    boolean togglePause();

    void sendNowPlayingPrompt(TextChannel channel);

    boolean shuffle();

    AudioTrack skip(Integer skip);
}
