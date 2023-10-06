package me.olliejonas.saltmarsh.embed.input.types;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.function.BiFunction;

public class InputEmbeds {

    public static BiFunction<String, MessageEmbed, InputButtonEmbed<Boolean>> BOOLEAN = (id, embed) ->
            InputButtonEmbed.builder(id, Boolean.class).embed(embed).option("Yes").option("No").build();
}
