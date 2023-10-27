package me.olliejonas.saltmarsh.embed.button;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class ButtonEmbedManagerImpl implements ButtonEmbedManager {

    private final Map<String, ButtonEmbed> buttonEmbedMap;

    public ButtonEmbedManagerImpl() {
        this(new HashMap<>());
    }

    public ButtonEmbedManagerImpl(Map<String, ButtonEmbed> buttonEmbedMap) {
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

    public void addToMap(String messageId, ButtonEmbed embed) {
        buttonEmbedMap.put(messageId, embed);
    }

    public InteractionResponses register(ButtonEmbed embed, Consumer<Message> onSuccess) {
        if (embed == null) return InteractionResponses.empty();

        MessageCreateBuilder builder = new MessageCreateBuilder().addEmbeds(embed);

        builder.setComponents(MiscUtils.batches(embed.getButtons(), 5).map(ActionRow::of).toList());

        return InteractionResponses.createData(builder.build(), false, onSuccess.andThen(message -> addToMap(message.getId(), embed)));
    }
}
