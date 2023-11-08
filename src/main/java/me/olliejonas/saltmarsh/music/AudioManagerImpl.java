package me.olliejonas.saltmarsh.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import me.olliejonas.saltmarsh.music.interfaces.GuildAudioManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;

public class AudioManagerImpl implements AudioManager {

    private final AudioPlayerManager audioPlayerManager;

    private final Map<String, GuildAudioManager> guildManagerMap;

    public AudioManagerImpl(AudioPlayerManager audioPlayerManager) {
        this(audioPlayerManager, new HashMap<>());
    }

    public AudioManagerImpl(AudioPlayerManager audioPlayerManager, Map<String, GuildAudioManager> guildManagerMap) {
        this.audioPlayerManager = audioPlayerManager;
        this.guildManagerMap = guildManagerMap;
    }

    @Override
    public GuildAudioManager getGuildManager(Guild guild) {
        if (!guildManagerMap.containsKey(guild.getId()))
            createManager(guild);

        return guildManagerMap.get(guild.getId());
    }

    public String playTrack(Member executor, String identifier) {
        Guild guild = executor.getGuild();
        GuildAudioManager manager = getGuildManager(guild);

        String connected = manager.connect(executor);
        if (connected != null) return connected;

        return addTrack(guild, identifier);
    }

    public String addTrack(Guild guild, String identifier) {
        GuildAudioManager guildAudioManager = getGuildManager(guild);
        // add transformation to spotify search here
        audioPlayerManager.loadItem(identifier, guildAudioManager.getTrackLoader());
        return "Successfully added track to queue!";
    }

    GuildAudioManager createManager(Guild guild) {
        AudioPlayer player = audioPlayerManager.createPlayer();
        GuildAudioManager manager = new GuildAudioManagerImpl(player, guild);
        player.addListener(new TrackScheduler(manager));

        guildManagerMap.put(guild.getId(), manager);
        guild.getAudioManager().setSendingHandler(manager.getSendHandler());

        return manager;
    }
}
