package me.olliejonas.saltmarsh.embed.select;

import lombok.Getter;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.DecoratedEmbed;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.*;
import java.util.function.Function;

@Getter
public class StringSelectEmbed extends MessageEmbed implements DecoratedEmbed {

    private final MessageEmbed embed;

    private final LinkedHashMap<SelectOption, Function<ClickContext, InteractionResponses>> actions;

    public record ActionSelectOption(SelectOption option, Function<ClickContext, InteractionResponses> action) {}
    public record ClickContext(Member clicker, SelectOption option, TextChannel channel,
                               RestAction<Message> message, String messageId) {}

    private StringSelectEmbed(MessageEmbed embed, LinkedHashMap<SelectOption, Function<ClickContext, InteractionResponses>> actions) {
        super(embed.getUrl(), embed.getTitle(), embed.getDescription(), embed.getType(), embed.getTimestamp(),
                embed.getColorRaw(), embed.getThumbnail(), embed.getSiteProvider(), embed.getAuthor(),
                embed.getVideoInfo(), embed.getFooter(), embed.getImage(), embed.getFields());
        this.embed = embed;
        this.actions = actions;
    }

    public List<SelectOption> options() {
        return new ArrayList<>(actions.keySet());
    }

    public static ButtonEmbed.Builder builder(EmbedBuilder builder) {
        return new ButtonEmbed.Builder(builder);
    }

    public static ButtonEmbed.Builder builder(MessageEmbed embed) {
        return new ButtonEmbed.Builder(embed);
    }

    public static class Builder {
        private final MessageEmbed embed;

        private final LinkedHashMap<SelectOption, Function<ClickContext, InteractionResponses>> actions;

        public Builder(EmbedBuilder builder) {
            this(builder.build());
        }

        public Builder(MessageEmbed embed) {
            this(embed, new LinkedHashMap<>());
        }

        public Builder(MessageEmbed embed, LinkedHashMap<SelectOption, Function<ClickContext, InteractionResponses>> actions) {
            this.embed = embed;
            this.actions = actions;
        }

        public Builder options(List<ActionSelectOption> buttons) {
            buttons.forEach(this::option);
            return this;
        }

        public Builder option(ActionSelectOption actionSelectOption) {
            String id = String.valueOf(actions.size());
            this.actions.put(actionSelectOption.option(), actionSelectOption.action());
            return this;
        }

        public StringSelectEmbed build() {
            return new StringSelectEmbed(embed, actions);
        }
    }
}
