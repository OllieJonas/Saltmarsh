package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.*;
import me.olliejonas.saltmarsh.util.embed.EmbedUtils;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbed;
import me.olliejonas.saltmarsh.util.embed.ButtonEmbedManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class TestButtonEmbedCommand extends Command {

    private final ButtonEmbedManager manager;

    public TestButtonEmbedCommand(ButtonEmbedManager manager) {
        super(CommandPermissions.ADMIN, "embed-interactable", "interactableembed", "interactable-embed", "iembed", "i-embed");
        this.manager = manager;
    }

    @Override
    public CommandInfo commandInfo() {
        return null;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, List<String> args, String aliasUsed) throws CommandFailedException {
        EmbedBuilder builder = EmbedUtils.standard();
        builder.setTitle("This is a title");
        builder.addField("wow", "this is fun! :) react for fun surprises >:)", false);
        ButtonEmbed embed = ButtonEmbed.builder(builder)
                .button(Emoji.fromUnicode("☺"),
                        context -> InteractionResponses.messageAsEmbed("I hope you're having a wonderful day, " +
                                context.clicker().getEffectiveName() + "!")).build();

        manager.send(channel, embed);

        return InteractionResponses.messageAsEmbed("Successfully tested embeds!");
    }
}
