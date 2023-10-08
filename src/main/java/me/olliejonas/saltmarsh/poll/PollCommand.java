package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.InputEmbed;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.embed.input.types.InputButton;
import me.olliejonas.saltmarsh.embed.input.types.InputRepeatingText;
import me.olliejonas.saltmarsh.embed.input.types.InputText;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PollCommand extends Command {

    private final PollEmbedManager manager;

    private final InputEmbedManager inputEmbedManager;

    public PollCommand(PollEmbedManager manager, InputEmbedManager inputEmbedManager) {
        super(CommandPermissions.ADMIN, "poll");
        this.manager = manager;
        this.inputEmbedManager = inputEmbedManager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "question", "the question you want to ask!"),
                new OptionData(OptionType.STRING, "options", "the possible options! (separated by either ' | ' or ' : '"),
                new OptionData(OptionType.BOOLEAN, "anonymous", "whether the voting should be anonymous! (defaults to true)"),
                new OptionData(OptionType.BOOLEAN, "singular", "whether users should be allowed to vote for multiple options or just one! (defaults to true)")
        );
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        if (args.size() < 2)
            return inputEmbedPoll(executor, channel);

        String question = args.get("question").getAsString();
        String options = args.get("options").getAsString();
        boolean anonymous = true;
        boolean singular = true;


        if (args.containsKey("anonymous"))
            anonymous = args.get("anonymous").getAsBoolean();

        if (args.containsKey("singular"))
            singular = args.get("singular").getAsBoolean();


        List<PollOption> pollOptions = Arrays.stream(options.split(" [|:] "))
                .map(PollOption::new)
                .toList();

        if (pollOptions.isEmpty()) throw CommandFailedException.badArgs(executor, this, "option 1 | option 2 | option ...");
        if (pollOptions.size() > 10) throw CommandFailedException.other("You can't have more than 10 options!", "no more than 10 options");

        buildAndSendPoll(executor, channel, question, pollOptions, anonymous, singular);

        return InteractionResponses.messageAsEmbed("Successfully created poll!", true);
    }

    private InteractionResponses inputEmbedPoll(Member executor, TextChannel channel) {

        String title = "Poll Wizard";

        InputEmbed embed = InputEmbed.builder()
                .step(new InputText<>("question", EmbedUtils.colour()
                        .setTitle(title)
                        .setDescription("What would you like the question to be?")
                        .build(), String.class))
                .step(InputRepeatingText.of("options", EmbedUtils.colour()
                        .setTitle(title)
                        .setDescription("Now enter the options you would like for this poll")
                        .build(), String.class))
                .step(InputButton.builder(executor.getGuild(), "anonymous", Boolean.class).embed(EmbedUtils.colour()
                        .setTitle(title)
                        .setDescription("Would you like this poll to be anonymous?")
                        .build())
                        .option("Yes")
                        .option("No")
                        .build())
                .step(InputButton.builder(executor.getGuild(), "singular", Boolean.class).embed(EmbedUtils.colour()
                                .setTitle(title)
                                .setDescription("Would you like this poll to be singular voting or multiple voting " +
                                        "(People can vote for only one option or multiple)?")
                                .build())
                        .option("Yes")
                        .option("No")
                        .build())

                .step(new InputText<>("targetChannel",
                        EmbedUtils.colour()
                                .setTitle(title)
                                .setDescription("Please tag the text channel you'd like this to be sent in!")
                                .build(), GuildChannel.class))

                .onCompletion(map -> {
                    String author = executor.getEffectiveName();
                    String question = (String) map.get("question");
                    List<String> options = (List<String>) map.get("options");
                    Boolean anonymous = (Boolean) map.get("anonymous");
                    Boolean singular = (Boolean) map.get("singular");
                    GuildChannel targetChannel = (GuildChannel) map.get("targetChannel");

                    List<PollOption> pollOptions = options.stream().map(PollOption::new).toList();

                    buildAndSendPoll(executor, (TextChannel) targetChannel, question, pollOptions, anonymous, singular);
                    return InteractionResponses.empty();
                })

                .completionPage(EmbedUtils.colour()
                        .setTitle(title)
                        .setDescription("Thanks for completing this wizard! The poll should be sent now!")
                        .build())

                .build();

        inputEmbedManager.send(executor, channel, embed);

        return InteractionResponses.empty();
    }

    public void buildAndSendPoll(Member executor, TextChannel channel, String question, List<PollOption> pollOptions,
                                 boolean anonymous, boolean singular) {

        PollEmbed embed = PollEmbed.builder(manager)
                .author(executor.getEffectiveName())
                .question(withQuestionMark(question))
                .options(pollOptions)
                .anonymous(anonymous)
                .singularVotes(singular)
                .build();


        manager.send(channel, embed);
    }

    private String withQuestionMark(String prompt) {
        return prompt.endsWith("?") ? prompt : prompt + "?";
    }
}
