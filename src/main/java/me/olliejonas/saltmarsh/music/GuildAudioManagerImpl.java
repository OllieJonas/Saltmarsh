package me.olliejonas.saltmarsh.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.music.interfaces.GuildAudioManager;
import me.olliejonas.saltmarsh.music.structures.AudioQueue;
import me.olliejonas.saltmarsh.music.structures.NowPlayingPrompt;
import me.olliejonas.saltmarsh.music.structures.TrackRepresentation;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class GuildAudioManagerImpl implements GuildAudioManager {

    static final Logger LOGGER = LoggerFactory.getLogger(GuildAudioManagerImpl.class);

    private final AudioPlayer player;

    private final Guild guild;

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

        LOGGER.info("playNext");

        return track;
    }

    public void sendNowPlayingPrompt(TextChannel channel) {
        NowPlayingPrompt prompt = new NowPlayingPrompt();
        nowPlayingPrompts.add(prompt);
        prompt.sendInitial(channel, currentTrack);
    }

    public AudioLoadResultHandler getTrackLoader(SlashCommandInteractionEvent event) {
        return new Loader(event);
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

    public int clearQueue() {
        return tracks.clearQueue();
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
    public boolean shuffle() {
        return !tracks.shuffle().isEmpty();
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

        clearQueue();
        this.currentTrack = null;

        return true;
    }

    public class Loader implements AudioLoadResultHandler {

        private final SlashCommandInteractionEvent event;

        public Loader(SlashCommandInteractionEvent event) {
            this.event = event;
        }
        @Override
        public void trackLoaded(AudioTrack track) {
            addTrack(track);
            replyToEvent(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.isSearchResult()) {
                AudioTrack track = playlist.getTracks().get(0);
                addTrack(track);
                replyToEvent(track);
            } else {
                playlist.getTracks().forEach(GuildAudioManagerImpl.this::addTrack);
                int size = playlist.getTracks().size();

                replyToEvent(size + " track" + (size != 1 ? "s" : ""));
            }
        }

        @Override
        public void noMatches() {
            LOGGER.warn("No matches found!");
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            LOGGER.error("Load failed", exception);
        }

        private void replyToEvent(AudioTrack track) {
            replyToEvent(new TrackRepresentation(track).representation());
        }

        private void replyToEvent(String added) {
            if (event != null)
                event.replyEmbeds(EmbedUtils.from("Added " + added + " to the queue!")).queue();
        }
    }
}
