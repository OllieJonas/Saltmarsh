package me.olliejonas.saltmarsh.embed.input.types.builders;

import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.util.List;

public class EntityMenuBuilder<T> extends SelectMenuBuilder<T, EntitySelectMenu, InputMenu.Entity<T>> {

    public EntityMenuBuilder(String identifier, Class<T> clazz) {
        super(identifier, clazz);
    }

    public EntityMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed) {
        super(identifier, clazz, embed);
    }

    public EntityMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed, List<EntitySelectMenu> selectMenus) {
        super(identifier, clazz, embed, selectMenus, __ -> true);
    }

    @Override
    public InputMenu.Entity<T> build() {
        return new InputMenu.Entity<>(identifier, clazz, embed, selectMenus, onOption, valid);
    }
}
