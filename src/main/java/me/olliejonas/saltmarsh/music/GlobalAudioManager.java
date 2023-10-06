package me.olliejonas.saltmarsh.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.olliejonas.saltmarsh.music.exceptions.NoVoiceChannelException;
import me.olliejonas.saltmarsh.music.exceptions.QueueException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalAudioManager {

    private final Map<Guild, GuildAudioManager> audioManagerMap;
    private final AudioPlayerManager audioPlayerManager;

    public GlobalAudioManager() {
        this(new DefaultAudioPlayerManager());

        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

    }

    public GlobalAudioManager(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
        this.audioManagerMap = new HashMap<>();
    }

    public Optional<GuildAudioManager> get(Guild guild) {
        return Optional.ofNullable(audioManagerMap.get(guild));
    }

    public boolean remove(Guild guild) {
        return audioManagerMap.remove(guild) != null;
    }

    public boolean joinAndPlay(Guild guild, TextChannel textChannel, Member executor, String track) throws NoVoiceChannelException {
        if (shouldJoin(guild))
            join(guild, executor, textChannel);

        return play(guild, executor.getUser(), track);
    }

    public void join(Guild guild, @NotNull Member member, TextChannel channel) throws NoVoiceChannelException {
        if (member.getVoiceState() == null)
            throw new NoVoiceChannelException("no voice state");


        GuildVoiceState state = member.getVoiceState();

        if (state.getChannel() == null)
            throw new NoVoiceChannelException("no voice channel");

        join(guild, state.getChannel().asVoiceChannel(), channel);
    }

    public VoiceChannel join(Guild guild, @NotNull VoiceChannel voiceChannel, TextChannel textChannel) {
        // already previously joined a channel, so we need to move it
        if (audioManagerMap.containsKey(guild) && !audioManagerMap.get(guild).getJoinedChannelId().equals(voiceChannel.getId())) {
            guild.getAudioManager().closeAudioConnection();
        }

        guild.getAudioManager().openAudioConnection(voiceChannel);
        GuildAudioManager guildAudioManager = new GuildAudioManager(guild, audioPlayerManager.createPlayer());
        guildAudioManager.init(voiceChannel, textChannel);

        guild.getAudioManager().setSendingHandler(guildAudioManager.getHandler());
        guild.getAudioManager().setSelfDeafened(true);

        audioManagerMap.put(guild, guildAudioManager);
        return voiceChannel;
    }

    public boolean play(Guild guild, User executor, String query) throws NoVoiceChannelException {
        if (!audioManagerMap.containsKey(guild))
            throw new NoVoiceChannelException();

        GuildAudioManager manager = audioManagerMap.get(guild);

        audioPlayerManager.loadItemOrdered(manager, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                manager.queue(track, executor);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult())
                    System.out.println("this was a yt search result!");
                else
                    playlist.getTracks().forEach(t -> manager.queue(t, executor));
            }

            @Override
            public void noMatches() {
                throw new QueueException(QueueException.Reason.NO_MATCHES);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                throw new QueueException(QueueException.Reason.TRACK_LOAD_FAILED, exception);
            }
        });

        return manager.getQueue().size() == 0;
    }

    private boolean shouldJoin(Guild guild) {
        return !audioManagerMap.containsKey(guild);
    }

    public void disconnect(Guild guild) {
        System.out.println("disconnected! :(");
        guild.getAudioManager().closeAudioConnection();
        audioManagerMap.remove(guild);
    }

    public void resume(Guild guild) {
        get(guild).ifPresentOrElse(GuildAudioManager::resume, () -> {throw new QueueException(QueueException.Reason.EMPTY_QUEUE);});
    }
}
