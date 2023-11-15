package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestButtonEmbedCommand extends Command {

    private final ButtonEmbedManager manager;

    public TestButtonEmbedCommand(ButtonEmbedManager manager) {
        super(CommandPermissions.ADMIN, "embed-button", "buttonembed", "button-embed", "bembed", "b-embed");
        this.manager = manager;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.empty();
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        EmbedBuilder builder = EmbedUtils.standard();
        builder.setTitle("This is a title");
        builder.addField("wow", "this is fun! :) react for fun surprises >:)", false);
        ButtonEmbed embed = ButtonEmbed.builder(builder)
                .button(Emoji.fromUnicode("☺"),
                        context -> InteractionResponses.messageAsEmbed("I hope you're having a wonderful day, " +
                                context.clicker().getEffectiveName() + "!")).build();

        return manager.register(embed);
    }
}
