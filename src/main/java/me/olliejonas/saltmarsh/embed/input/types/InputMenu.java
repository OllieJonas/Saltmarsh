package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.input.types.builders.ButtonBuilder;
import me.olliejonas.saltmarsh.embed.input.types.builders.EntityMenuBuilder;
import me.olliejonas.saltmarsh.embed.input.types.builders.StringMenuBuilder;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

// I don't know if this is the cleanest thing I've ever done in Java or the worst thing I've ever done in Java
public sealed interface InputMenu<T, R extends ItemComponent> extends InputCandidate<T>
        permits InputMenu.String, InputMenu.Entity, InputMenu.Button {

    List<R> components();

    default int length() { return 1; };

    default MessageCreateData compile() {
        List<ActionRow> batched = new ArrayList<>(MiscUtils.batches(components(), length())
                .map(ActionRow::of).toList());

        batched.add(ActionRow.of(EXIT_BUTTON));

        return new MessageCreateBuilder().setEmbeds(embed())
                .setComponents(batched)
                .build();
    }

    record Entity<T>(java.lang.String identifier, Class<T> clazz, MessageEmbed embed, List<EntitySelectMenu> components, Predicate<T> valid)
            implements InputMenu<T, EntitySelectMenu> {
        public static final Set<Class<?>> VALID_CLASSES = Set.of(Role.class, User.class, GuildChannel.class);

        public static final Predicate<GuildChannel> TEXT_ONLY = channel -> channel instanceof TextChannel;

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

    record String<T>(java.lang.String identifier, Class<T> clazz, MessageEmbed embed, List<StringSelectMenu> components, Predicate<T> valid)
            implements InputMenu<T, StringSelectMenu> {

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
                     List<net.dv8tion.jda.api.interactions.components.buttons.Button> components, Predicate<T> valid)
            implements InputMenu<T, net.dv8tion.jda.api.interactions.components.buttons.Button> {

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
    }
}
