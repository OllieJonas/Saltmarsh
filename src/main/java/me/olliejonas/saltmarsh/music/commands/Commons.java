package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.exceptions.NoVoiceChannelException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Commons {

    public static InteractionResponses joinAndPlay(GlobalAudioManager manager, TextChannel channel, Member executor, String input) {
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
