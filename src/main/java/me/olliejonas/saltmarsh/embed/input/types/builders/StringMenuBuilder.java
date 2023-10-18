package me.olliejonas.saltmarsh.embed.input.types.builders;

import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

public class StringMenuBuilder<T> extends SelectMenuBuilder<T, StringSelectMenu, InputMenu.String<T>> {

    public StringMenuBuilder(String identifier, Class<T> clazz) {
        super(identifier, clazz);
    }

    public StringMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed) {
        super(identifier, clazz, embed);
    }

    public StringMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed, List<StringSelectMenu> selectMenus) {
        super(identifier, clazz, embed, selectMenus, __ -> true);
    }

    @Override
    public InputMenu.String<T> build() {
        return new InputMenu.String<>(identifier, clazz, embed, selectMenus, onOption, valid);
    }
}
