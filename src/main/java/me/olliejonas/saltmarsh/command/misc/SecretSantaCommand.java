package me.olliejonas.saltmarsh.command.misc;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import me.olliejonas.saltmarsh.embed.wizard.types.StepRepeatingText;
import me.olliejonas.saltmarsh.embed.wizard.types.StepText;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

public class SecretSantaCommand extends Command {

    static final String DEFAULT_SUBSTITUTE_TEXT = "{target}";

    private final WizardEmbedManager manager;

    private final String substituteText;

    private final boolean developerMode;

    public SecretSantaCommand(WizardEmbedManager manager, boolean developerMode) {
        this(manager, DEFAULT_SUBSTITUTE_TEXT, developerMode);
    }

    public SecretSantaCommand(WizardEmbedManager manager, String substituteText, boolean developerMode) {
        super(CommandPermissions.EVENTS, "secret-santa");

        this.manager = manager;
        this.substituteText = substituteText;
        this.developerMode = developerMode;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("Secret santa!");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return manager.register(channel, WizardEmbed.builder()
                        .step(StepText.of("text", "What message would you like to send each user?",
                                "Use `" + substituteText + "` to substitute the name of the person that user " +
                                        "received as a Secret Santa.\n\n" +
                                        "For example: \"Your Secret Santa is " + substituteText + "!\"", String.class))

                .step(StepRepeatingText.of("members",
                        "Secret Santa",
                        "Please tag the members you'd like to include for secret santa!",
                        Member.class,
                        valid()))

                .onCompletion(ctx -> doSecretSanta((String) ctx.get("text"), (Collection<Member>) ctx.get("members"), channel))
                .build());
    }

    private BiPredicateWithContext<Member, StepCandidate<Member>> valid() {
        return (member, self) -> {
            if (member.getId().equals(Constants.SELF_USER_ID))
                return new Tuple2<>(false, "As flattered as I am, you can't have me in the Secret Santa ...");

            if (member.getUser().isBot() && !developerMode)
                return new Tuple2<>(false, "Robots don't celebrate Christmas smh");

            return new Tuple2<>(true, "");
        };
    }

    private InteractionResponses doSecretSanta(String text, Collection<Member> members, TextChannel channel) {
        List<Member> copy = new ArrayList<>(members);
        Collections.shuffle(copy);


        Map<Member, Member> reversed = new HashMap<>();

        Set<Tuple2<Member, Member>> santaAlloc = members.stream()
                .map(member -> {
                    Member target = copy.stream()
                            .filter(val -> !val.equals(member))  // not a -> a
                            .filter(val -> !reversed.containsKey(val))  // not b -> a, c -> a
                            .filter(val -> !(reversed.containsKey(member) && reversed.get(member) == val))  // not a -> b, b -> a
                            .findFirst()
                            .orElseThrow();

                    reversed.put(target, member);

                    return new Tuple2<>(member, target);
                })
                .collect(Collectors.toSet());

        if (developerMode && channel != null)
            channel.sendMessageEmbeds(EmbedUtils.from("Secret Santa (DEBUG)", santaAlloc.stream()
                    .map(tuple -> tuple.v1().getEffectiveName() + " -> " + tuple.v2().getEffectiveName())
                    .collect(Collectors.joining("\n")))).queue();

        santaAlloc.stream()
                .map(tuple -> tuple.map2(target -> userText(text, target)))
                .map(tuple -> tuple.map1(Member::getUser))
                .filter(tuple -> !tuple.v1().isBot())
                .forEach(tuple -> tuple.v1().openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessageEmbeds(EmbedUtils.from("Secret Santa", tuple.v2()))).queue());

        return InteractionResponses.embed("Secret Santa",
                "Everyone should have been DMed their Secret Santa!");
    }

    private String userText(String text, Member target) {
        return text.replace(substituteText, target.getAsMention());
    }
}
