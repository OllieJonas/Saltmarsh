package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.exceptions.QueueException;
import me.olliejonas.saltmarsh.util.embed.PaginatedAudioQueueEmbed;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbed;
import me.olliejonas.saltmarsh.util.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class QueueCommand extends Command {

    private final static int DEFAULT_ITEMS_PER_PAGE = 10;

    private final GlobalAudioManager audioManager;

    private final PaginatedEmbedManager embedManager;

    public QueueCommand(GlobalAudioManager audioManager, PaginatedEmbedManager embedManager) {
        super("queue");
        this.audioManager = audioManager;
        this.embedManager = embedManager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.empty();
    }

    @Override
    public List<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "url", "The URL for a track (or playlist)!"));
    }


    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        return switch (args.size()) {
            case 0 -> queue(executor.getGuild(), channel);
            case 1 -> Commons.joinAndPlay(audioManager, channel, executor, args.get(0));
            default -> throw CommandFailedException.badArgs(executor, this, "track-url (optional)");
        };
    }

    private InteractionResponses queue(Guild guild, TextChannel channel) {
        PaginatedEmbed queueEmbed = PaginatedAudioQueueEmbed.build(embedManager,
                audioManager.get(guild)
                        .orElseThrow(() ->
                                CommandFailedException.other("The queue is currently empty! :(",
                                        "tbh, the bot isn't even initialised. im just tryna be nice here :/"))
                        .getQueue(), DEFAULT_ITEMS_PER_PAGE);
        try {
            embedManager.send(channel, queueEmbed, () -> {throw CommandFailedException.other(
                    "The queue is currently empty!",
                    "ok bot is initted, the queue is actually empty wow");});
        } catch (QueueException ex) {
            throw ex.asFailed();
        }




        return InteractionResponses.messageAsEmbed("Successfully displayed queue!");
    }
}