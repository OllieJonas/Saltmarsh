package me.olliejonas.saltmarsh.embed.input.types.builders;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.EntryContext;
import me.olliejonas.saltmarsh.embed.input.types.InputMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class SelectMenuBuilder<T, R extends SelectMenu, V extends InputMenu<T, R>> {

    protected final String identifier;

    protected final Class<T> clazz;

    protected List<R> selectMenus;

    protected MessageEmbed embed;

    protected Consumer<EntryContext<T>> onOption;

    protected Predicate<T> valid;

    public SelectMenuBuilder(String identifier, Class<T> clazz) {
        this(identifier, clazz, null, new ArrayList<>(), __ -> true);
    }

    public SelectMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed) {
        this(identifier, clazz, embed, new ArrayList<>(), __ -> true);
    }

    public SelectMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed, List<R> selectMenus, Predicate<T> valid) {
        this.identifier = identifier;
        this.clazz = clazz;
        this.selectMenus = selectMenus;
        this.embed = embed;
        this.onOption = (__) -> {};
        this.valid = valid;
    }

    public SelectMenuBuilder<T, R, V> embed(String title, String description) {
        return embed(EmbedUtils.colour().setTitle(title).setDescription(description).build());
    }

    public SelectMenuBuilder<T, R, V> embed(MessageEmbed embed) {
        this.embed = embed;
        return this;
    }

    public SelectMenuBuilder<T, R, V> selectMenu(R selectMenu) {
        this.selectMenus.add(selectMenu);
        return this;
    }

    public SelectMenuBuilder<T, R, V> selectMenus(List<R> selectMenus) {
        this.selectMenus = selectMenus;
        return this;
    }

    public SelectMenuBuilder<T, R, V> valid(Predicate<T> predicate) {
        this.valid = predicate;
        return this;
    }

    public SelectMenuBuilder<T, R, V> valid(Collection<Predicate<T>> predicates) {
        return valid(predicates.stream().reduce(Predicate::and).orElse(__ -> true));
    }

    public SelectMenuBuilder<T, R, V> onOption(Consumer<EntryContext<T>> onOption) {
        this.onOption = onOption;
        return this;
    }

    public abstract V build();
}
