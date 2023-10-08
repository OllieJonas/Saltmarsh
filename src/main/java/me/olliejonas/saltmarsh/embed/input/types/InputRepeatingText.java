package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record InputRepeatingText<T>(String identifier, MessageEmbed embed, Class<T> clazz, String exitText)
        implements InputCandidate<T> {

    public static <T> InputRepeatingText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new InputRepeatingText<>(identifier, embed, clazz, "Next");
    }

    public static <T> InputRepeatingText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), clazz);
    }

    @Override
    public MessageCreateData compile() {
        return buildEmbed(Collections.emptyList());
    }

    public MessageCreateData buildEmbed(List<T> items) {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", String.format("""
                        Please type your answer into the same channel that this is sent in!
                        Press "%s" when you're done!
                        
                        """, exitText), false)
                .addField("Current Input", items.stream().map(Object::toString).collect(Collectors.joining(", ")), false)
                .build();
        return new MessageCreateBuilder().setEmbeds(newEmbed).setActionRow(Button.success("_", exitText), EXIT_BUTTON).build();
    }

    @Override
    public Function<String, Integer> skip() {
        return __ -> 0;
    }
}
