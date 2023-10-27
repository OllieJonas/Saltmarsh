package me.olliejonas.saltmarsh.embed.wizard.types.builders;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.EntryContext;
import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jooq.lambda.tuple.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public abstract class SelectMenuBuilder<T, R extends SelectMenu, V extends StepMenu<T, R>> {

    protected final String identifier;

    protected final Class<T> clazz;

    protected List<R> selectMenus;

    protected MessageEmbed embed;

    protected Consumer<EntryContext<T>> onOption;

    protected BiPredicateWithContext<T, StepCandidate<T>> valid;

    public SelectMenuBuilder(String identifier, Class<T> clazz) {
        this(identifier, clazz, null, new ArrayList<>(), (__, ___) -> new Tuple2<>(true, ""));
    }

    public SelectMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed) {
        this(identifier, clazz, embed, new ArrayList<>(), (__, ___) -> new Tuple2<>(true, ""));
    }

    public SelectMenuBuilder(String identifier, Class<T> clazz, MessageEmbed embed, List<R> selectMenus, BiPredicateWithContext<T, StepCandidate<T>> valid) {
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

    public SelectMenuBuilder<T, R, V> valid(BiPredicateWithContext<T, StepCandidate<T>> predicate) {
        this.valid = predicate;
        return this;
    }

    public SelectMenuBuilder<T, R, V> valid(Collection<BiPredicateWithContext<T, StepCandidate<T>>> predicates) {
        return valid(predicates.stream().reduce(BiPredicateWithContext::and).orElse((__, ___) -> new Tuple2<>(true, "")));
    }

    public SelectMenuBuilder<T, R, V> onOption(Consumer<EntryContext<T>> onOption) {
        this.onOption = onOption;
        return this;
    }

    public abstract V build();
}
