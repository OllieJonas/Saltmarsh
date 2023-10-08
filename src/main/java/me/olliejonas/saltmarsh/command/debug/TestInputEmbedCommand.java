package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.input.InputEmbed;
import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import me.olliejonas.saltmarsh.embed.input.types.InputButton;
import me.olliejonas.saltmarsh.embed.input.types.InputText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestInputEmbedCommand extends Command {

    private final InputEmbedManager manager;

    public TestInputEmbedCommand(InputEmbedManager manager) {
        super(CommandPermissions.ADMIN, "inputembed");
        this.manager = manager;
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {

        InputEmbed embed = InputEmbed.builder()
                .step(new InputText<>("name",
                        new EmbedBuilder().setTitle("Hi").setDescription("Enter a name!").build(),
                        String.class))
                .step(new InputText<>("integer", new EmbedBuilder()
                        .setTitle("Hi (2)")
                        .setDescription("Enter an integer!")
                        .build(),
                        Integer.class))
                .step(InputButton.builder(executor.getGuild(), "verb")
                        .option("danced")
                        .option("skated")
                        .embed(new EmbedBuilder()
                                .setTitle("Hi (3)")
                                .setDescription("Select a verb!").build()).build())
                .step(new InputText<>("user", new EmbedBuilder().setTitle("Hi (4)").setDescription("Now tag a user!").build(), Member.class))
                .onCompletion(map -> {
                    String name = (String) map.get("name");
                    Integer integer = (Integer) map.get("integer");
                    String verb = (String) map.get("verb");
                    Member user = (Member) map.get("user");

                    return InteractionResponses.messageAsEmbed(name + " " + verb + " with " + user.getAsMention() + " " + integer + " times!", true);
                })
                .build();

        manager.send(executor, channel, embed);

        return InteractionResponses.empty();
    }
}
