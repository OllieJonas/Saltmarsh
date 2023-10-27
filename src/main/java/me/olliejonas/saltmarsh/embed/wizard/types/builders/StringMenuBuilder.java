package me.olliejonas.saltmarsh.embed.wizard.types.builders;

import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;

public class StringMenuBuilder<T> extends SelectMenuBuilder<T, StringSelectMenu, StepMenu.String<T>> {

    public StringMenuBuilder(String identifier, Class<T> clazz) {
        super(identifier, clazz);
    }

    public StringMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed) {
        super(identifier, clazz, embed);
    }

    public StringMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed, List<StringSelectMenu> selectMenus) {
        super(identifier, clazz, embed, selectMenus, (__, ___) -> new Tuple2<>(true, ""));
    }

    @Override
    public StepMenu.String<T> build() {
        return new StepMenu.String<>(identifier, clazz, embed, selectMenus, onOption, valid);
    }
}
