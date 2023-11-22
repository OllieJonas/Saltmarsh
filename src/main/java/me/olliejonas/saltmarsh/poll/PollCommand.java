package me.olliejonas.saltmarsh.poll;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.CommonWizardMenus;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.embed.wizard.types.StepRepeatingText;
import me.olliejonas.saltmarsh.embed.wizard.types.StepText;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PollCommand extends Command {

    private final PollManager manager;

    private final WizardEmbedManager wizardEmbedManager;

    public PollCommand(PollManager manager, WizardEmbedManager wizardEmbedManager) {
        super(CommandPermissions.EVENTS, "poll");

        this.manager = manager;
        this.wizardEmbedManager = wizardEmbedManager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Create polls! " +
                "(Use command without arguments for Wizard)");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "question", "the question you want to ask!"),
                new OptionData(OptionType.STRING, "options", "the possible options! (separated by either ' | ' or ' : '"),
                new OptionData(OptionType.BOOLEAN, "anonymous", "whether the voting should be anonymous! (defaults to true)"),
                new OptionData(OptionType.BOOLEAN, "singular", "whether users should be allowed to vote for multiple options or just one! (defaults to true)"),
                new OptionData(OptionType.BOOLEAN, "text-represented", "whether the poll options should be shown as numbers or text (defaults to false)"),
                new OptionData(OptionType.CHANNEL, "target-channel", "the channel that you'd like to send the poll to! (defaults to this one)"),
                new OptionData(OptionType.BOOLEAN, "should-notify-channel", "whether the users should be tagged when the poll is sent (defaults to false)")
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        if (!args.containsKey("question") || !args.containsKey("options"))
            return inputEmbedPoll(executor, channel);

        String question = args.get("question").getAsString();
        String options = args.get("options").getAsString();
        boolean anonymous = true;
        boolean singular = true;
        boolean textRepresented = false;
        TextChannel targetChannel = channel;
        boolean notifyChannel = false;


        if (args.containsKey("anonymous"))
            anonymous = args.get("anonymous").getAsBoolean();

        if (args.containsKey("singular"))
            singular = args.get("singular").getAsBoolean();

        if (args.containsKey("textRepresented"))
            singular = args.get("textRepresented").getAsBoolean();

        if (args.containsKey("target-channel")) {
            GuildChannel gChannel = args.get("target-channel").getAsChannel();
            if (!(gChannel instanceof TextChannel tChannel)) return InteractionResponses.error("Please specify a text channel to send the poll to!");
            targetChannel = tChannel;
        }

        if (args.containsKey("notifyChannel"))
            singular = args.get("singular").getAsBoolean();


        List<PollOption> pollOptions = Arrays.stream(options.split(" [|:] "))
                .map(PollOption::new)
                .toList();

        if (pollOptions.isEmpty()) throw CommandFailedException.badArgs(executor, this, "option 1 | option 2 | option ...");
        if (pollOptions.size() > 10) textRepresented = true;

        buildAndSendPoll(executor, targetChannel, question, pollOptions, anonymous, singular, textRepresented, notifyChannel).queue(null, targetChannel);
        return InteractionResponses.messageAsEmbed("Successfully sent poll!", false);
    }

    private InteractionResponses inputEmbedPoll(Member executor, TextChannel channel) {
        String title = "Poll Wizard";
        String nextText = "Next";

        WizardEmbed embed = WizardEmbed.builder()
                .step(StepText.of("question", title,
                        "What would you like the question to be?", String.class,
                        (input, __) -> new Tuple2<>(input.length() <= PollManager.QUESTION_MAX_LENGTH,
                                "The question cannot be longer than " + PollManager.QUESTION_MAX_LENGTH + "!")))
                .step(CommonWizardMenus.YES_NO("yesno", title,
                        "Is this poll a Yes/No poll? (The only two options are Yes and No)", 2, 1))
                .step(StepRepeatingText.of("options", title,
                        "Now enter the options you would like for this poll", String.class, this::checkOptionsAreValid))
                .step(CommonWizardMenus.YES_NO("anonymous", title,
                        "Would you like this poll to be anonymous?"))
                .step(CommonWizardMenus.YES_NO("singular", title,
                        "Would you like this poll to be singular? (Only allow one vote per person)"))
                .step(CommonWizardMenus.YES_NO("textRepresented", title,
                        "Would you like the voting options to be represented by the prompt itself (as opposed to numbers)?"))
                .step(StepMenu.Entity.builder("targetChannel", GuildChannel.class)
                        .embed(title,
                                "Now please select the text channel you'd like to send this poll to!")
                        .selectMenu(EntitySelectMenu.create("targetChannel",
                                EntitySelectMenu.SelectTarget.CHANNEL)
                                .build())
                        .valid(StepMenu.Entity.TEXT_ONLY())
                        .build())
                .step(CommonWizardMenus.YES_NO("notifyChannel", title,
                        "Would you like to notify the channel of this poll being sent? (Tag everyone in it)"))

                .onCompletion(map -> {
                    String author = executor.getEffectiveName();
                    String question = (String) map.get("question");
                    Boolean yesNo = (Boolean) map.get("yesno");
                    List<String> options = yesNo ? List.of("Yes", "No") : (List<String>) map.get("options");
                    Boolean anonymous = (Boolean) map.get("anonymous");
                    Boolean singular = (Boolean) map.get("singular");
                    Boolean textRepresented = (Boolean) map.get("textRepresented");
                    GuildChannel targetChannel = (GuildChannel) map.get("targetChannel");
                    Boolean notifyChannel = (Boolean) map.get("notifyChannel");

                    if (!yesNo)
                        options.remove(nextText);

                    List<PollOption> pollOptions = options.stream().map(PollOption::new).toList();

                    if (pollOptions.size() > 10 && !textRepresented)
                        textRepresented = true;

                    buildAndSendPoll(executor, (TextChannel) targetChannel, question, pollOptions,
                            anonymous, singular, textRepresented, notifyChannel).queue(null, (TextChannel) targetChannel);

                    return InteractionResponses.empty();
                })

                .completionPage(EmbedUtils.colour()
                        .setTitle(title)
                        .setDescription("Thanks for completing this wizard! The poll should be sent now!")
                        .build())
                .build();

        return wizardEmbedManager.register(executor, channel, embed);
    }

    public InteractionResponses buildAndSendPoll(Member executor, TextChannel channel, String question, List<PollOption> pollOptions,
                                 boolean anonymous, boolean singular, boolean textRepresented, boolean notifyChannel) {

        PollEmbed embed = PollEmbed.builder()
                .author(executor.getEffectiveName())
                .question(withQuestionMark(question))
                .options(pollOptions)
                .anonymous(anonymous)
                .singularVotes(singular)
                .textRepresented(textRepresented)
                .build();


        return manager.send(executor, channel, embed, notifyChannel);
    }

    private Tuple2<Boolean, String> checkOptionsAreValid(String currInput, StepCandidate<String> stepCandidate) {
        StepRepeatingText<String> curr = (StepRepeatingText<String>) stepCandidate;

        if (currInput.equals("Here in my garage"))
            return new Tuple2<>(false, "Just bought this, uh, new Lamborghini here. Fun to drive up here in the Hollywood Hills.");

        if (currInput.contains(PollManager.SQL_POLL_SPLIT))
            return new Tuple2<>(false, "Your option cannot contain the character \"" + PollManager.SQL_POLL_SPLIT + "\" !");

        return String.join("", curr.options()).length() + currInput.length() < PollManager.MAX_COMBINED_OPTION_LENGTH(curr.options().size()) ?
                new Tuple2<>(true, "") : new Tuple2<>(false,
                "The total number of characters in your options + the number of different options cannot exceed "
                        + PollManager.SQL_OPTION_LENGTH + " characters! (This is an internal storage thing, congrats for hitting it!)");
    }

    private String withQuestionMark(String prompt) {
        return prompt.endsWith("?") ? prompt : prompt + "?";
    }
}
