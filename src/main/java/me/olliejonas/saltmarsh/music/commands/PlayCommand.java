package me.olliejonas.saltmarsh.music.commands;

import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;

public class PlayCommand extends Command {

    private final GlobalAudioManager manager;

    public PlayCommand(GlobalAudioManager manager) {
        super("play");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return CommandInfo.empty();
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "track", "The URL of a track to play"));
    }

    @Override
    public InteractionResponses execute(Member executor,
                                        TextChannel channel, List<String> args,
                                        String aliasUsed) throws CommandFailedException {
        return switch (args.size()) {
            case 0 -> resume(executor.getGuild());
            case 1 -> Commons.joinAndPlay(manager, channel, executor, args.get(0));
            default -> throw CommandFailedException.badArgs(executor, this, "track-url (optional)");
        };
    }

    private InteractionResponses resume(Guild guild) {
        manager.resume(guild);
        return InteractionResponses.messageAsEmbed("Successfully resumed track!");
    }
}
