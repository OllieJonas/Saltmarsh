package me.olliejonas.saltmarsh.embed.wizard.types.builders;

import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;

public class EntityMenuBuilder<T> extends SelectMenuBuilder<T, EntitySelectMenu, StepMenu.Entity<T>> {

    public EntityMenuBuilder(String identifier, Class<T> clazz) {
        super(identifier, clazz);
    }

    public EntityMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed) {
        super(identifier, clazz, embed);
    }

    public EntityMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed, List<EntitySelectMenu> selectMenus) {
        super(identifier, clazz, embed, selectMenus, (__, ___) -> new Tuple2<>(true, ""));
    }

    @Override
    public StepMenu.Entity<T> build() {
        return new StepMenu.Entity<>(identifier, clazz, embed, selectMenus, onOption, valid);
    }
}
