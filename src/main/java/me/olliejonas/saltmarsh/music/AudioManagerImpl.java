package me.olliejonas.saltmarsh.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import me.olliejonas.saltmarsh.music.interfaces.GuildAudioManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioManagerImpl implements AudioManager {

    static final Logger LOGGER = LoggerFactory.getLogger(AudioManagerImpl.class);

    private final AudioPlayerManager audioPlayerManager;

    private final Map<String, GuildAudioManager> guildManagerMap;

    private final SpotifyWrapped spotify;


    public AudioManagerImpl(AudioPlayerManager audioPlayerManager, SpotifyWrapped spotify) {
        this(audioPlayerManager, spotify, new HashMap<>());
    }

    public AudioManagerImpl(AudioPlayerManager audioPlayerManager, SpotifyWrapped spotify, Map<String, GuildAudioManager> guildManagerMap) {
        this.audioPlayerManager = audioPlayerManager;
        this.spotify = spotify;
        this.guildManagerMap = guildManagerMap;
    }

    @Override
    public GuildAudioManager getGuildManager(Guild guild) {
        if (!guildManagerMap.containsKey(guild.getId()))
            createManager(guild);

        return guildManagerMap.get(guild.getId());
    }

    public String addTrack(Member executor, String identifier) throws IOException, ParseException, SpotifyWebApiException {
        Guild guild = executor.getGuild();
        GuildAudioManager manager = getGuildManager(guild);

        String connected = manager.connect(executor);
        if (connected != null) return connected;

        return addTrack(guild, identifier);
    }

    public String addTrack(Guild guild, String identifier) throws IOException, ParseException, SpotifyWebApiException {
        GuildAudioManager guildAudioManager = getGuildManager(guild);

        List<String> tracks = Collections.emptyList();

        if (identifier.contains("spotify")) {
            tracks = spotify.transform(identifier);


            if (tracks == null) {
                return "Spotify is currently disabled! :(";
            }

            LOGGER.debug("Track Info (from Spotify): " + String.join(", ", tracks));

            if (tracks.isEmpty()) {
                return "This listening method isn't currently supported! (Only supports tracks, albums & playlists :( )";
            }
        }

        if (tracks.isEmpty()) {
            tracks = Collections.singletonList(identifier);
        }

        tracks = withYtSearch(tracks);

        int size = tracks.size();

        tracks.forEach(track -> audioPlayerManager.loadItem(track, guildAudioManager.getTrackLoader()));

        return "Successfully added " + size + " track" + (size == 1 ? "" : "s") + " to the queue!";
    }

    GuildAudioManager createManager(Guild guild) {
        AudioPlayer player = audioPlayerManager.createPlayer();
        GuildAudioManager manager = new GuildAudioManagerImpl(player, guild);
        player.addListener(new TrackScheduler(manager));

        guildManagerMap.put(guild.getId(), manager);
        guild.getAudioManager().setSendingHandler(manager.getSendHandler());

        return manager;
    }


    private boolean isUrl(String input) {
        return input.startsWith("http://") || input.startsWith("https://");
    }

    private List<String> withYtSearch(List<String> input) {
        return input.stream().map(track -> {
            if (isUrl(track))
                return track;

            return track.startsWith("ytsearch:") ? track : "ytsearch:" + track;
        }).toList();
    }
}
