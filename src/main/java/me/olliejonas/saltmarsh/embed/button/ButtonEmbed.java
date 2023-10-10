package me.olliejonas.saltmarsh.embed.button;

import lombok.Getter;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.DecoratedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.*;
import java.util.function.Function;

@Getter
public class ButtonEmbed extends MessageEmbed implements DecoratedEmbed {

    public record ActionButton(Button button, Function<ClickContext, InteractionResponses> action) {}

    public record ClickContext(Member clicker, Button button, TextChannel channel, RestAction<Message> message, String messageId) {
        public int index() {
            return Integer.parseInt(Objects.requireNonNull(button.getId()));
        }
    }

    private final MessageEmbed embed;

    private final Map<String, Function<ClickContext, InteractionResponses>> actions;

    private final List<Button> buttons;

    private ButtonEmbed(MessageEmbed embed, List<Button> buttons, Map<String, Function<ClickContext, InteractionResponses>> actions) {
        super(embed.getUrl(), embed.getTitle(), embed.getDescription(), embed.getType(), embed.getTimestamp(),
                embed.getColorRaw(), embed.getThumbnail(), embed.getSiteProvider(), embed.getAuthor(),
                embed.getVideoInfo(), embed.getFooter(), embed.getImage(), embed.getFields());
        this.embed = embed;
        this.buttons = buttons;
        this.actions = actions;
    }

    public static Builder builder(EmbedBuilder builder) {
        return new Builder(builder);
    }

    public static Builder builder(MessageEmbed embed) {
        return new Builder(embed);
    }

    public static class Builder {
        private final MessageEmbed embed;

        private Map<String, Function<ClickContext, InteractionResponses>> actions;

        private final List<Button> buttons;

        public Builder(EmbedBuilder builder) {
            this(builder.build());
        }

        public Builder(MessageEmbed embed) {
            this(embed, new ArrayList<>(), new HashMap<>());
        }

        public Builder(MessageEmbed embed, List<Button> buttons, Map<String, Function<ClickContext, InteractionResponses>> actions) {
            this.embed = embed;
            this.buttons = buttons;
            this.actions = actions;
        }

        public Builder buttons(List<ActionButton> buttons) {
            buttons.forEach(this::button);
            return this;
        }

        public Builder button(ActionButton actionButton) {
            String id = String.valueOf(actions.size());
            this.actions.put(id, actionButton.action());
            this.buttons.add(actionButton.button().withId(id));
            return this;
        }

        public Builder button(String emojiUnicode, Function<ClickContext, InteractionResponses> action) {
            return button(Emoji.fromUnicode(emojiUnicode), action);
        }
        public Builder button(Emoji emoji, Function<ClickContext, InteractionResponses> action) {
            return button(new ActionButton(Button.primary(String.valueOf(actions.size()), emoji), action));
        }
        public Builder button(Button button, Function<ClickContext, InteractionResponses> action) {
            return button(new ActionButton(button, action));
        }

        public ButtonEmbed build() {
            return new ButtonEmbed(embed, buttons, actions);
        }
    }
}
