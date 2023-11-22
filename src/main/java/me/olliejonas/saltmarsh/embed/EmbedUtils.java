package me.olliejonas.saltmarsh.embed;

import lombok.experimental.UtilityClass;
import me.olliejonas.saltmarsh.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EmbedUtils {

    public EmbedBuilder colour(EmbedBuilder builder) {
        builder.setColor(Constants.APP_COLOUR);
        return builder;
    }

    public static MessageEmbed from(String message) {
        return standard().setDescription(message).build();
    }

    public static MessageEmbed from(String title, String description) {
        return fromAsBuilder(title, description).build();
    }

    public static EmbedBuilder fromAsBuilder(String title, String description) {
        return standard().setTitle(title).setDescription(description);
    }

    public static MessageEmbed error(String message) {
        return essentials().setColor(Color.RED).setDescription(message).build();
    }

    public EmbedBuilder standard() {
        return colour(footer(new EmbedBuilder()));
    }

    public EmbedBuilder essentials() {
        return author(footer(new EmbedBuilder()));
    }

    public EmbedBuilder minimal() {
        return colour(footer(new EmbedBuilder()));
    }

    public EmbedBuilder authored() { return author(colour(new EmbedBuilder())); }

    public EmbedBuilder colour() {
        return colour(new EmbedBuilder());
    }

    public MessageEmbed colour(String title, String description) {
        return colour().setTitle(title).setDescription(description).build();
    }

    public EmbedBuilder author(EmbedBuilder builder) {
        builder.setAuthor(Constants.APP_TITLE);
        return builder;
    }

    public EmbedBuilder footer(EmbedBuilder builder) {
        builder.setFooter(footer());
        return builder;
    }

    public String footer() {
        return "Sent at " + DateTimeFormatter.RFC_1123_DATE_TIME.format(LocalDateTime.now().atOffset(ZoneOffset.UTC));
    }
}
