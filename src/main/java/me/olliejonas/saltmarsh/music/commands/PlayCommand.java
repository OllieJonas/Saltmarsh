package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;

public class PlayCommand extends Command {

    private final AudioManager manager;

    private final Map<String, Map<TextChannel, Long>> nowPlayingPromptChannels;

    public PlayCommand(AudioManager manager) {
        super(CommandPermissions.MUSIC, "play");

        this.manager = manager;
        this.nowPlayingPromptChannels = new WeakConcurrentHashMap<>();
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(MUSIC) Adds a track to the queue");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "link", "Either a YouTube or Spotify link to play", true)
        );
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        String link = args.get("link").getAsString();

        // now playing prompt
        Guild guild = executor.getGuild();
        if (!nowPlayingPromptChannels.containsKey(guild.getId()))
            nowPlayingPromptChannels.put(guild.getId(), new HashMap<>());

        Map<TextChannel, Long> channels = nowPlayingPromptChannels.get(guild.getId());

        if (!channels.containsKey(channel) || Math.abs(channels.get(channel) - System.currentTimeMillis()) <= 1_800_000) { // around
            channels.put(channel, System.currentTimeMillis());
            manager.sendNowPlayingPrompt(guild, channel);
        }

        // add ytsearch transformation here
        if (!isUrl(link))
            link = withYtSearch(link);

        return InteractionResponses.messageAsEmbed(manager.playTrack(executor, link), true);
    }

    private boolean isUrl(String input) {
        return input.startsWith("http://") || input.startsWith("https://");
    }

    private String withYtSearch(String input) {
        return input.startsWith("ytsearch:") ? input : "ytsearch:" + input;
    }
}
