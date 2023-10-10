package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Predicate;

public record InputText<T>(String identifier, MessageEmbed embed, Class<T> clazz, Predicate<T> valid) implements InputCandidate<T> {

    public static <T> InputText<T> of(String identifier, String title, String description, Class<T> clazz, Predicate<T> valid) {
        return new InputText<>(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), clazz, valid);
    }

    public static <T> InputText<T> of(String identifier, String title, String description, Class<T> clazz) {
        return of(identifier, title, description, clazz, __ -> true);
    }

    public static <T> InputText<T> of(String identifier, MessageEmbed embed, Class<T> clazz) {
        return new InputText<>(identifier, embed, clazz, __ -> true);
    }

    public MessageCreateData compile() {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", "Please type your answer into the same channel that this is sent in!", true)
                .build();
        return new MessageCreateBuilder().setEmbeds(newEmbed).addActionRow(EXIT_BUTTON).build();
    }
}
