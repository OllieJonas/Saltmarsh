package me.olliejonas.saltmarsh.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import me.olliejonas.saltmarsh.music.interfaces.GuildAudioManager;
import me.olliejonas.saltmarsh.music.structures.AudioQueue;
import me.olliejonas.saltmarsh.music.structures.NowPlayingPrompt;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashSet;
import java.util.Set;

public class GuildAudioManagerImpl implements GuildAudioManager {

    private final AudioPlayer player;

    private final Guild guild;

    @Getter
    private final Loader trackLoader;

    @Getter
    private final AudioQueue<AudioTrack> tracks;

    private final Set<NowPlayingPrompt> nowPlayingPrompts;


    private AudioChannelUnion activeVoiceChannel;

    private AudioTrack currentTrack;

    private boolean currentlyPaused;

    public GuildAudioManagerImpl(AudioPlayer player, Guild guild) {
        this.player = player;
        this.guild = guild;
        this.currentlyPaused = false;

        this.nowPlayingPrompts = new HashSet<>();
        this.trackLoader = new Loader();
        this.tracks = new AudioQueue<>();
    }

    @Override
    public AudioSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    @Override
    public AudioTrack playNext() {
        AudioTrack track = tracks.poll();
        currentTrack = track;

        if (track != null)
            player.playTrack(track);

        nowPlayingPrompts.forEach(prompt -> prompt.onNextItem(track));

        return track;
    }

    public void sendNowPlayingPrompt(TextChannel channel) {
        NowPlayingPrompt prompt = new NowPlayingPrompt();
        nowPlayingPrompts.add(prompt);
        prompt.sendInitial(channel, currentTrack);
    }

    public String connect(Member member) {
        GuildVoiceState state = member.getVoiceState();
        if (state == null) return "Internal error! (getVoiceState in connect returned null, contact Ollie!)";

        AudioChannelUnion channel = state.getChannel();

        if (channel == null) return "You need to be in a voice channel to start playing music!";

        return connect(channel);
    }

    void addTrack(AudioTrack item) {
        tracks.add(item);

        if (currentTrack == null)
            playNext();
    }

    public String connect(AudioChannelUnion channel) {
        AudioManager manager = guild.getAudioManager(); // JDA import, not mine

        if (this.activeVoiceChannel != null && this.activeVoiceChannel != channel) {
            manager.closeAudioConnection();
        }

        manager.setSendingHandler(getSendHandler());
        manager.setSelfDeafened(true);

        guild.getAudioManager().openAudioConnection(channel);
        this.activeVoiceChannel = channel;

        return null;
    }

    // returns the new paused value
    public boolean togglePause() {

        currentlyPaused = !currentlyPaused;
        player.setPaused(currentlyPaused);
        return currentlyPaused;
    }

    @Override
    public AudioTrack skip(Integer skip) {
        AudioTrack nextMinusOne = tracks.skip(skip - 1);

        AudioTrack next = playNext();

        if (next == null) player.stopTrack();

        return next;
    }

    public boolean stop() {
        player.stopTrack();
        return activeVoiceChannel != null;
    }

    public boolean disconnect() {
        if (activeVoiceChannel == null) return false;

        guild.getAudioManager().closeAudioConnection();
        this.activeVoiceChannel = null;

        return true;
    }

    public class Loader implements AudioLoadResultHandler {

        @Override
        public void trackLoaded(AudioTrack track) {
            addTrack(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            playlist.getTracks().forEach(GuildAudioManagerImpl.this::addTrack);
        }

        @Override
        public void noMatches() {

        }

        @Override
        public void loadFailed(FriendlyException exception) {

        }
    }
}
