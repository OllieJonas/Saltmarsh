package me.olliejonas.saltmarsh.embed.button;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.MiscUtils;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Optional;
import java.util.function.Consumer;

public class ButtonEmbedManager {

    private final WeakConcurrentHashMap<String, ButtonEmbed> buttonEmbedMap;

    public ButtonEmbedManager() {
        this(new WeakConcurrentHashMap<>());
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

    public InteractionResponses send(ButtonEmbed embed) {
        return send(embed, (__) -> {});
    }

    public InteractionResponses send(ButtonEmbed embed, Consumer<Message> onSuccess) {
        MessageCreateBuilder builder = new MessageCreateBuilder().addEmbeds(embed);

        builder.setComponents(MiscUtils.batches(embed.getButtons(), 5).map(ActionRow::of).toList());

        return InteractionResponses.createData(builder.build(), false, onSuccess.andThen(message -> buttonEmbedMap.put(message.getId(), embed)));
    }
}
