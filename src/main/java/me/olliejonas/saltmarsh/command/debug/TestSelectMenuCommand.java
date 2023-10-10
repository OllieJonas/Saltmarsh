package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Map;

public class TestSelectMenuCommand extends Command {

    public TestSelectMenuCommand() {
        super(CommandPermissions.ADMIN, "select-menu");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {

        MessageCreateData string = new MessageCreateBuilder().setEmbeds(EmbedUtils.colour().setTitle("Food")
                .setImage("https://www.hepper.com/wp-content/uploads/2022/11/maltipoo-dog-walking-at-the-park_Irsan-Ianushis_Shutterstock.jpg")
                .setDescription("What's your favourite food? (String Select)")
                .build()).addActionRow(
                        StringSelectMenu.create("choose-food")
                                .addOption("Pizza", "pizza", "This is another description!")
                                .addOptions(SelectOption.of("Hamburger", "hamburger")
                                        .withDescription("This is a description!").withEmoji(Emoji.fromUnicode("\uD83C\uDF54")).withDefault(true))
                                .build())
                .build();

        MessageCreateData entity = new MessageCreateBuilder().setEmbeds(EmbedUtils.colour().setTitle("Food")
                        .setImage("https://www.hepper.com/wp-content/uploads/2022/11/maltipoo-dog-walking-at-the-park_Irsan-Ianushis_Shutterstock.jpg")
                        .setDescription("channel?")
                        .build()).addActionRow(
                        EntitySelectMenu.create("channel", EntitySelectMenu.SelectTarget.CHANNEL).build())
                .build();

        channel.sendMessage(string).queue();
        channel.sendMessage(entity).queue();

        return InteractionResponses.empty();
    }
}
