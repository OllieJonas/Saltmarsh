package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.util.MiscUtils;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;

public record InputButton<T>(String identifier, MessageEmbed embed, List<Button> buttons, Class<T> clazz)
        implements InputCandidate<T> {



    public MessageCreateData compile() {
        List<Button> newButtons = new ArrayList<>(buttons);
        newButtons.add(EXIT_BUTTON);

        List<ActionRow> batched = new ArrayList<>(MiscUtils.batches(newButtons, 5)
                .map(ActionRow::of).toList());

        return new MessageCreateBuilder().setEmbeds(embed)
                .setComponents(batched)
                .build();
    }

    public static InputButton<Boolean> YES_NO(String identifier, String title, String description) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build());
    }

    public static InputButton<Boolean> YES_NO(String identifier, MessageEmbed embed) {
        return new InputButton<>(identifier,
                embed,
                List.of(Button.primary("1", "Yes"), Button.primary("2", "No")), Boolean.class);
    }

    public static Builder<String> builder(Guild guild, String identifier) {
        return new Builder<>(guild, identifier, String.class);
    }

    public static <T> Builder<T> builder(Guild guild, String identifier, Class<T> clazz) {
        return new Builder<>(guild, identifier, clazz);
    }

    public static <T> Builder<T> builder(Guild guild, String identifier, MessageEmbed embed, Class<T> clazz) {
        return new Builder<>(guild, identifier, embed, clazz);
    }

    public static class Builder<T> {

        private final Guild guild;

        private final String identifier;
        private final Class<T> clazz;

        private MessageEmbed embed;

        private List<Button> buttons;


        public Builder(Guild guild, String identifier, Class<T> clazz) {
            this(guild, identifier, null, clazz, new ArrayList<>());
        }

        public Builder(Guild guild, String identifier, MessageEmbed embed, Class<T> clazz) {
            this(guild, identifier, embed, clazz, new ArrayList<>());
        }

        public Builder(Guild guild, String identifier, MessageEmbed embed, Class<T> clazz, List<Button> buttons) {
            this.guild = guild;
            this.identifier = identifier;
            this.embed = embed;
            this.clazz = clazz;
            this.buttons = buttons;
        }

        public Builder<T> embed(MessageEmbed embed) {
            this.embed = embed;
            return this;
        }

        public Builder<T> option(String text) {
            return option(Button.primary(String.valueOf(buttons.size()), text));
        }

        public Builder<T> option(Emoji emoji) {
            if (clazz != String.class && clazz != Boolean.class)
                throw new IllegalArgumentException("can only use either String or Booleans with Emojis!");

            return option(Button.primary(String.valueOf(buttons.size()), emoji));
        }

        public Builder<T> option(Button button) {
            if (StringToTypeConverter.cantExtendedCast(guild, button.getLabel(), clazz))
                throw new IllegalArgumentException("you need to be able to cast your button value!");

            buttons.add(button.withId(String.valueOf(buttons.size())));
            return this;
        }

        public Builder<T> options(List<Button> options) {
            if (options.stream().anyMatch(opt -> StringToTypeConverter.cantExtendedCast(guild, opt.getLabel(), clazz)))
                throw new IllegalArgumentException("you need to be able to cast your buttons!");

            this.buttons = options;
            return this;
        }

        public InputButton<T> build() {
            return new InputButton<>(identifier, embed, buttons, clazz);
        }
    }
}
