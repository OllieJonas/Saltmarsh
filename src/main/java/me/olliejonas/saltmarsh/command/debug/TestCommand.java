package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.embed.PaginatedEmbedManager;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.poll.PollEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestCommand extends Command {

    private final ButtonEmbedManager buttonEmbedManager;

    private final PaginatedEmbedManager paginatedEmbedManager;

    private final GlobalAudioManager globalAudioManager;

    private final PollEmbedManager pollEmbedManager;

    public TestCommand(ButtonEmbedManager buttonEmbedManager,
                       PaginatedEmbedManager paginatedEmbedManager,
                       PollEmbedManager pollEmbedManager,
                       GlobalAudioManager globalAudioManager) {
        super(CommandPermissions.ADMIN, "test");
        this.buttonEmbedManager = buttonEmbedManager;
        this.paginatedEmbedManager = paginatedEmbedManager;
        this.pollEmbedManager = pollEmbedManager;
        this.globalAudioManager = globalAudioManager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Test commands for debugging purposes (ADMIN)");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(FORCED_SUBCOMMAND_ARG);
    }

    @Override
    public void addSubCommands() {
        addSubCommand(new TestPlayCommand(globalAudioManager));
        addSubCommand(new TestPaginatedEmbedCommand(paginatedEmbedManager));
        addSubCommand(new TestButtonEmbedCommand(buttonEmbedManager));
        addSubCommand(new TestItemizedEmbedCommand(paginatedEmbedManager));
        addSubCommand(new TestPollCommand(pollEmbedManager));
        addSubCommand(new TestFailureCommand());
        addSubCommand(new TestEphemeralCommand());
        addSubCommand(new TestBotJoinsCommand(globalAudioManager));
        addSubCommand(new TestInputEmbedCommand());
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.empty();
    }

    @Override
    public boolean shouldRegisterAsSlashCommand() {
        return true;
    }
}
