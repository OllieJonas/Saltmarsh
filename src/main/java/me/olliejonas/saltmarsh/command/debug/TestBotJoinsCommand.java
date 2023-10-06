package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.music.exceptions.NoVoiceChannelException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class TestBotJoinsCommand extends Command {

    private final GlobalAudioManager manager;

    public TestBotJoinsCommand(GlobalAudioManager manager) {
        super(CommandPermissions.ADMIN, "bot-join", "join", "botjoin");

        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {

        Guild guild = executor.getGuild();

        try {
            manager.join(guild, executor, channel);
        } catch (NoVoiceChannelException e) {
            throw CommandFailedException.other("I can't find a channel to join! :(", e.getMessage());
        }
//
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                manager.disconnect(guild);
//            }
//        }, 5000);

        return InteractionResponses.messageAsEmbed("Test running! You should see the bot join, then leave after 5 seconds", true);
    }
}
