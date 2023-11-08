package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import me.olliejonas.saltmarsh.embed.button.derivations.PaginatedEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.music.interfaces.AudioManager;
import me.olliejonas.saltmarsh.poll.PollManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestCommand extends Command {

    private final AudioManager audioManager;

    private final ButtonEmbedManager buttonEmbedManager;

    private final PaginatedEmbedManager paginatedEmbedManager;

    private final WizardEmbedManager wizardEmbedManager;

    private final PollManager pollManager;

    private final boolean shouldRegister;


    public TestCommand(boolean shouldRegister, AudioManager audioManager, ButtonEmbedManager buttonEmbedManager,
                       PaginatedEmbedManager paginatedEmbedManager,
                       PollManager pollManager,
                       WizardEmbedManager wizardEmbedManager) {

        super(CommandPermissions.ADMIN, "test");
        this.shouldRegister = shouldRegister;
        this.audioManager = audioManager;
        this.buttonEmbedManager = buttonEmbedManager;
        this.paginatedEmbedManager = paginatedEmbedManager;
        this.wizardEmbedManager = wizardEmbedManager;
        this.pollManager = pollManager;
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
        addSubCommand(new TestPaginatedEmbedCommand(paginatedEmbedManager));
        addSubCommand(new TestButtonEmbedCommand(buttonEmbedManager));
        addSubCommand(new TestItemizedEmbedCommand(paginatedEmbedManager));
        addSubCommand(new TestPollCommand(pollManager));
        addSubCommand(new TestFailureCommand());
        addSubCommand(new TestEphemeralCommand());
        addSubCommand(new TestInputEmbedCommand(wizardEmbedManager));
        addSubCommand(new TestSelectMenuCommand());
        addSubCommand(new TestPlayCommand(audioManager));
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.empty();
    }

    @Override
    public boolean shouldRegisterAsSlashCommand() {
        return this.shouldRegister;
    }
}
