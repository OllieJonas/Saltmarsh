package me.olliejonas.saltmarsh.kingdom.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.embed.wizard.types.StepRepeatingText;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.KingdomGameRegistry;
import me.olliejonas.saltmarsh.kingdom.RoleAllocation;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MTGKingdomCommand extends Command {

    private final WizardEmbedManager wizardEmbedManager;

    private final KingdomGameRegistry registry;

    private final boolean developerMode;

    public MTGKingdomCommand(KingdomGameRegistry registry, WizardEmbedManager wizardEmbedManager, boolean developerMode) {
        super(CommandPermissions.EVENTS, "kingdom");

        this.registry = registry;
        this.wizardEmbedManager = wizardEmbedManager;
        this.developerMode = developerMode;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(KINGDOM) Create a game of Kingdom!");
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return wizardEmbedManager.register(channel, embed(channel));
    }

    private WizardEmbed embed(TextChannel channel) {
        String title = "Kingdom Wizard";

        return WizardEmbed.builder()
                .step(StepMenu.Button.builder("selectMethod")
                        .embed(title, "How would you like to select your players?")
                        .buttons(Button.primary("tag", "By tagging users"), Button.primary("vc", "By choosing a voice channel"))
                        .onOption(ctx -> ctx.self().setSkip(ctx.result().equals("By tagging users") ? 1 : 2)).build())
                .step(StepRepeatingText.of("taggedUsers", title, "Please tag the users you'd like to include in your game!", Member.class,
                        ((result, self) -> {
                            if (!developerMode && result.getUser().isBot())
                                return new Tuple2<>(false, "The user cannot be a bot!");

                            if (!developerMode && !result.getUser().hasPrivateChannel())
                                return new Tuple2<>(false, "The user must have access to a private Discord channel!");

                            return new Tuple2<>(true, "");
                        }),
                        2))
                .step(StepMenu.Entity.builder("targetVoiceChannel", GuildChannel.class)
                        .embed(title, "Please select a voice channel to allocate roles!")
                        .selectMenu(EntitySelectMenu.create("targetVoiceChannel", EntitySelectMenu.SelectTarget.CHANNEL).build())
                        .valid(StepMenu.Entity.VOICE_ONLY())
                        .build())
                .onCompletion(map -> {
                    Collection<Member> members = null;

                    if (map.containsKey("targetVoiceChannel"))
                        members = ((VoiceChannel) map.get("targetVoiceChannel")).getMembers();

                    if (map.containsKey("taggedUsers"))
                        members = (List<Member>) map.get("taggedUsers");

                    if (members == null)
                        throw new IllegalStateException("members is null when it really shouldn't be!");

                    try {
                        KingdomGame game = registry.startGame(members, channel, createRoleAllocation());
                        Tuple2<MessageEmbed, FileUpload> announcement = game.getRoleAllocationStrategy().announcement(game);

                        return InteractionResponses.embed(announcement.v1());
                    } catch (IllegalStateException e) {
                        return InteractionResponses.error(e.getMessage());

                    } catch (Exception e) {
                        return InteractionResponses.error("Error " + e.getMessage() + "!\n" + MiscUtils.shortenedStackTrace(e, 10));
                    }
                }).build();
    }

    private RoleAllocation.Strategy createRoleAllocation() {
        return createRoleAllocation(new Random());
    }

    private RoleAllocation.Strategy createRoleAllocation(Random random) {
        float rand = random.nextFloat();

        if (rand <= 0.1)
            return new RoleAllocation.FakeJesterGame();
        else
            return new RoleAllocation.Default();
    }
}
