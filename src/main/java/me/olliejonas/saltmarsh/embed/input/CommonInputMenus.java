package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public class CommonInputMenus {

    public static InputMenu.Button<Boolean> YES_NO(String identifier,
                                                   String title,
                                                   String description) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build());
    }

    public static InputMenu.Button<Boolean> YES_NO(String identifier, MessageEmbed embed) {
        return new InputMenu.Button<>(identifier, Boolean.class, embed, List.of(
                Button.primary("1", "Yes"),
                Button.primary("2", "No")), __ -> true);
    }
}
