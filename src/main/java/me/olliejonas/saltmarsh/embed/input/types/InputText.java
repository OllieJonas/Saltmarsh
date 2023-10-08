package me.olliejonas.saltmarsh.embed.input.types;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public record InputText<T>(String identifier, MessageEmbed embed, Class<T> clazz) implements InputCandidate<T> {

    public MessageCreateData compile() {
        MessageEmbed newEmbed = new EmbedBuilder(embed)
                .addField("", "Please type your answer into the same channel that this is sent in!", true)
                .build();
        return new MessageCreateBuilder().setEmbeds(newEmbed).build();
    }
}
