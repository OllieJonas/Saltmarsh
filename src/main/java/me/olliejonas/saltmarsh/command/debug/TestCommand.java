package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.music.GlobalAudioManager;
import me.olliejonas.saltmarsh.poll.PollEmbedManager;
import me.olliejonas.saltmarsh.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.embed.PaginatedEmbedManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;

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
        return List.of(new OptionData(OptionType.STRING, "test", "The thing to test!"));
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
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed("go get some help gurr");
    }

    @Override
    public boolean registerAsSlashCommand() {
        return true;
    }
}
