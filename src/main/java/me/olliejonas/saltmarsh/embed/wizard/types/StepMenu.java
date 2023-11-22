package me.olliejonas.saltmarsh.embed.wizard.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.EntryContext;
import me.olliejonas.saltmarsh.embed.wizard.types.builders.ButtonBuilder;
import me.olliejonas.saltmarsh.embed.wizard.types.builders.EntityMenuBuilder;
import me.olliejonas.saltmarsh.embed.wizard.types.builders.StringMenuBuilder;
import me.olliejonas.saltmarsh.util.MiscUtils;
import me.olliejonas.saltmarsh.util.functional.BiPredicateWithContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jooq.lambda.tuple.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// I don't know if this is the cleanest thing I've ever done in Java or the worst thing I've ever done in Java
public sealed interface StepMenu<T, R extends ItemComponent> extends StepCandidate<T>
        permits StepMenu.String, StepMenu.Entity, StepMenu.Button {

    List<R> components();

    default int length() { return 1; };

    default MessageCreateData compile(boolean showExitButton) {
        List<ActionRow> batched = new ArrayList<>(MiscUtils.batches(components(), length())
                .map(ActionRow::of).toList());

        if (showExitButton)
            batched.add(ActionRow.of(EXIT_BUTTON));

        return new MessageCreateBuilder().setEmbeds(embed())
                .setComponents(batched)
                .build();
    }

    record Entity<T>(java.lang.String identifier, Class<T> clazz, MessageEmbed embed, List<EntitySelectMenu> components,
                     Consumer<EntryContext<T>> onOption, BiPredicateWithContext<T, StepCandidate<T>> valid)
            implements StepMenu<T, EntitySelectMenu> {
        public static final Set<Class<?>> VALID_CLASSES = Set.of(Role.class, User.class, GuildChannel.class);

        public static <T> BiPredicateWithContext<GuildChannel, StepCandidate<T>> TEXT_ONLY()
        { return (channel, __) -> new Tuple2<>(channel instanceof TextChannel, "You can only choose text channels!"); }

        public static <T> BiPredicateWithContext<GuildChannel, StepCandidate<T>> VOICE_ONLY() {
            return (channel, __) -> new Tuple2<>(channel instanceof VoiceChannel, "You can only select voice channels!");
        }
        public Entity {
            if (!VALID_CLASSES.contains(clazz))
                throw new IllegalArgumentException("clazz must be in one of the valid classes!");
        }

        public static EntityMenuBuilder<java.lang.String> builder(java.lang.String identifier) {
            return new EntityMenuBuilder<>(identifier, java.lang.String.class);
        }

        public static <T> EntityMenuBuilder<T> builder(java.lang.String identifier, Class<T> clazz) {
            return new EntityMenuBuilder<>(identifier, clazz);
        }

        public static <T> EntityMenuBuilder<T> builder(java.lang.String identifier, MessageEmbed embed, Class<T> clazz) {
            return new EntityMenuBuilder<>(identifier, clazz, embed);
        }
    }

    record String<T>(java.lang.String identifier, Class<T> clazz, MessageEmbed embed, List<StringSelectMenu> components,
                     Consumer<EntryContext<T>> onOption, BiPredicateWithContext<T, StepCandidate<T>> valid)
            implements StepMenu<T, StringSelectMenu> {

        public static StringMenuBuilder<java.lang.String> builder(java.lang.String identifier) {
            return new StringMenuBuilder<>(identifier, java.lang.String.class);
        }

        public static <T> StringMenuBuilder<T> builder(java.lang.String identifier, Class<T> clazz) {
            return new StringMenuBuilder<>(identifier, clazz);
        }

        public static <T> StringMenuBuilder<T> builder(java.lang.String identifier, MessageEmbed embed, Class<T> clazz) {
            return new StringMenuBuilder<>(identifier, clazz, embed);
        }
    }

    record Button<T>(java.lang.String identifier, Class<T> clazz, MessageEmbed embed,
                     List<net.dv8tion.jda.api.interactions.components.buttons.Button> components,
                     Consumer<EntryContext<T>> onOption, BiPredicateWithContext<T, StepCandidate<T>> valid, AtomicInteger skipAmount)
            implements StepMenu<T, net.dv8tion.jda.api.interactions.components.buttons.Button> {

        public static <T> Button<T> of(java.lang.String identifier, Class<T> clazz, MessageEmbed embed,
                                       List<net.dv8tion.jda.api.interactions.components.buttons.Button> components,
                                       Consumer<EntryContext<T>> onOption, BiPredicateWithContext<T, StepCandidate<T>> valid) {
            return new Button<>(identifier, clazz, embed,  components, onOption, valid, new AtomicInteger(1));
        }

        @Override
        public void setSkip(int skip) {
            skipAmount().set(skip);
        }

        @Override
        public int skip() {
            return skipAmount.get();
        }

        @Override
        public int length() {
            return 5;
        }

        public static ButtonBuilder<java.lang.String> builder(java.lang.String identifier) {
            return new ButtonBuilder<>(identifier, java.lang.String.class);
        }

        public static <T> ButtonBuilder<T> builder(java.lang.String identifier, Class<T> clazz) {
            return new ButtonBuilder<>(identifier, clazz);
        }

        public static <T> ButtonBuilder<T> builder(java.lang.String identifier, MessageEmbed embed, Class<T> clazz) {
            return new ButtonBuilder<>(identifier, embed, clazz);
        }

        public static ButtonBuilder<java.lang.String> builder(java.lang.String identifier,
                                                   java.lang.String title, java.lang.String description) {
            return builder(identifier, title, description, java.lang.String.class);
        }

        public static <T> ButtonBuilder<T> builder(java.lang.String identifier,
                                                   java.lang.String title, java.lang.String description, Class<T> clazz) {
            return builder(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), clazz);
        }
    }
}
