package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.exceptions.QueueException;
import me.olliejonas.saltmarsh.music.ItemizedAudioQueueEmbed;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbed;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Map;

public class QueueCommand extends AudioCommand {

    private final static int DEFAULT_ITEMS_PER_PAGE = 10;

    private final PaginatedEmbedManager embedManager;

    public QueueCommand(GlobalAudioManager audioManager, PaginatedEmbedManager embedManager) {
        super(audioManager, "queue");
        this.embedManager = embedManager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Displays the music track queue");
    }

    @Override
    public List<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "url", "The URL for a track (or playlist)!", false));
    }


    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, Map<String, OptionMapping> args,
                                        String aliasUsed) throws CommandFailedException {
        return switch (args.size()) {
            case 0 -> queue(executor.getGuild(), channel);
            case 1 -> Commons.joinAndPlay(manager, channel, executor, args.get("url").getAsString());
            default -> throw CommandFailedException.badArgs(executor, this, "track-url (optional)");
        };
    }

    private InteractionResponses queue(Guild guild, TextChannel channel) {
        PaginatedEmbed queueEmbed = ItemizedAudioQueueEmbed.build(embedManager,
                manager.get(guild)
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
