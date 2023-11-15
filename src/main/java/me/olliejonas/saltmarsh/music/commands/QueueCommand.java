package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbed;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import me.olliejonas.saltmarsh.music.interfaces.GuildAudioManager;
import me.olliejonas.saltmarsh.music.structures.QueueEmbed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class QueueCommand extends Command {
    private final PaginatedEmbedManager paginatedEmbedManager;

    private final AudioManager manager;

    public QueueCommand(PaginatedEmbedManager paginatedEmbedManager, AudioManager manager) {
        super(CommandPermissions.MUSIC, "queue");
        this.paginatedEmbedManager = paginatedEmbedManager;
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(MUSIC) Shows the current queue");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        GuildAudioManager audioManager = manager.getGuildManager(channel.getGuild());
        PaginatedEmbed embed = new QueueEmbed(audioManager.getTracks()).toPaginatedEmbed();
        return paginatedEmbedManager.register(embed);
    }
}
