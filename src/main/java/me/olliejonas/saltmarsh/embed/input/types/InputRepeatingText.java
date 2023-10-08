package me.olliejonas.saltmarsh.embed.input.types;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Function;

public record InputRepeatingText<T>(String identifier, MessageEmbed embed, Class<T> clazz, String exitText)
        implements InputCandidate<T> {

    public static <T> InputRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new InputRepeatingText<>(identifier, embed, clazz, "Next");
    }

    @Override
    public MessageCreateData compile() {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", "Please type your answer into the same channel that this is sent in!", true)
                .addField("", String.format("Press \"%s\" when you're done!", exitText), false)
                .build();
        return new MessageCreateBuilder().setEmbeds(newEmbed).setActionRow(Button.success("_", exitText)).build();
    }

    @Override
    public Function<String, Integer> skip() {
        return __ -> 0;
    }
}
