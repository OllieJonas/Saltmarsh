package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.GuildAudioManager;
import me.olliejonas.saltmarsh.music.exceptions.NoVoiceChannelException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AudioCommand extends Command {

    protected final GlobalAudioManager manager;

    public AudioCommand(GlobalAudioManager manager, String alias) {
        this(manager, CommandPermissions.ALL, alias, Collections.emptySet(), Collections.emptyMap());
    }

    public AudioCommand(GlobalAudioManager manager, String primaryAlias, String... aliases) {
        this(manager, CommandPermissions.ALL, primaryAlias, Set.of(aliases), Collections.emptyMap());
    }

    public AudioCommand(GlobalAudioManager manager, CommandPermissions permissions, String primaryAlias) {
        this(manager, permissions, primaryAlias, Collections.emptySet(), Collections.emptyMap());
    }

    public AudioCommand(GlobalAudioManager manager, CommandPermissions permissions, String primaryAlias, String... aliases) {
        this(manager, permissions, primaryAlias, Set.of(aliases), Collections.emptyMap());
    }

    public AudioCommand(GlobalAudioManager manager, CommandPermissions permissions, String primaryAlias, Set<String> aliases) {
        this(manager, permissions, primaryAlias, aliases, Collections.emptyMap());
    }

    public AudioCommand(GlobalAudioManager manager, CommandPermissions permissions, String primaryAlias,
                        Set<String> aliases, Map<String, Command> subCommands) {
        super(permissions, primaryAlias, aliases, subCommands);
        this.manager = manager;
    }

    protected boolean join(Guild guild, Member member, TextChannel channel) {
        try {
            manager.join(guild, member, channel);
            return true;
        } catch (NoVoiceChannelException e) {
            return false;
        }
    }


    protected GuildAudioManager from(GlobalAudioManager manager, Guild guild) {
        return manager.get(guild).orElseThrow(() ->
                CommandFailedException.other("I'm not currently in a voice channel! :(",
                        "no guild found in globalaudiomanager"));
    }

    protected InteractionResponses joinAndPlay(GlobalAudioManager manager, TextChannel channel, Member executor, String input) {
        Guild guild = executor.getGuild();
        System.out.println("joined!");
        try {
            return manager.joinAndPlay(guild, channel, executor, input) ?
                    InteractionResponses.messageAsEmbed("Started playing your track!", true) :
                    InteractionResponses.messageAsEmbed("Your track has been queued!", true);
        } catch (NoVoiceChannelException e) {
            throw CommandFailedException.other("I was unable to find a voice channel to join! :(", e.getMessage());
        }
    }
}
