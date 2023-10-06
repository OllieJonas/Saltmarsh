package me.olliejonas.saltmarsh.util.embed;

import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ButtonEmbedManager {

    private final WeakConcurrentHashMap<String, ButtonEmbed> buttonEmbedMap;

    public ButtonEmbedManager() {
        this(new WeakConcurrentHashMap<>(TimeUnit.HOURS.toMillis(24)));
    }

    public ButtonEmbedManager(WeakConcurrentHashMap<String, ButtonEmbed> buttonEmbedMap) {
        this.buttonEmbedMap = buttonEmbedMap;
    }

    public Optional<ButtonEmbed> get(String id) {
        return Optional.ofNullable(buttonEmbedMap.get(id));
    }

    public boolean exists(String id) {
        return buttonEmbedMap.containsKey(id);
    }

    public void remove(String id) {
        if (exists(id))
            buttonEmbedMap.remove(id);
    }

    public void send(TextChannel channel, ButtonEmbed embed) {
        send(channel, embed, (__) -> {});
    }

    public void send(TextChannel channel, ButtonEmbed embed, Consumer<? super Message> onSuccess) {
        MessageCreateBuilder builder = new MessageCreateBuilder().addEmbeds(embed);

        if (!embed.getButtons().isEmpty())
            builder.setActionRow(embed.getButtons());

        channel.sendMessage(builder.build()).queue((message) -> {
            onSuccess.accept(message);
            buttonEmbedMap.put(message.getId(), embed);
        });
    }
}
