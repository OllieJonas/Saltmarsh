package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbed;
import me.olliejonas.saltmarsh.embed.wizard.WizardEmbedManager;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.embed.wizard.types.StepText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Map;

public class TestInputEmbedCommand extends Command {

    private final WizardEmbedManager manager;

    public TestInputEmbedCommand(WizardEmbedManager manager) {
        super(CommandPermissions.ADMIN, "inputembed");
        this.manager = manager;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {

        WizardEmbed embed = WizardEmbed.builder()
                .step(StepText.of("name",
                        new EmbedBuilder().setTitle("Hi").setDescription("Enter a name!").build(),
                        String.class))
                .step(StepMenu.Button.builder("amount", Integer.class)
                        .embed("Hi", "How many times did this happen?")
                        .button("1")
                        .button("2")
                        .button("3")
                        .build())
                .step(StepMenu.String.builder("verb")
                        .selectMenu(StringSelectMenu.create("verb")
                                .addOption("Danced", "danced")
                                .addOption("Skated", "skated")
                                .build())
                        .embed(new EmbedBuilder()
                                .setTitle("Hi (3)")
                                .setDescription("Select a verb!").build()).build())
                .step(StepText.of("user", new EmbedBuilder().setTitle("Hi (4)").setDescription("Now tag a user!").build(), Member.class))
                .step(StepMenu.Entity.builder("channel", GuildChannel.class).embed("Hi", "Now select a channel!").selectMenu(EntitySelectMenu.create("channel",
                        EntitySelectMenu.SelectTarget.CHANNEL).build()).build())
                .onCompletion(map -> {
                    String name = (String) map.get("name");
                    Integer integer = (Integer) map.get("amount");
                    String verb = (String) map.get("verb");
                    Member user = (Member) map.get("user");
                    GuildChannel guildChannel = (GuildChannel) map.get("channel");

                    return InteractionResponses.messageAsEmbed(name + " " + verb + " with " + user.getAsMention() + " " + integer + " times in " + guildChannel.getName() + "!", true);
                })
                .build();

        return InteractionResponses.empty();
    }
}
