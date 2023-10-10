package me.olliejonas.saltmarsh.embed.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;

public class ButtonEmbedListener extends ListenerAdapter {

    /*
        onMessageReactionAdd(@Nonnull MessageReactionAddEvent event)
        onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event)
        onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event)
        onMessageReactionRemoveEmoji(@Nonnull MessageReactionRemoveEmojiEvent event)
     */

    private final ButtonEmbedManager manager;

    public ButtonEmbedListener(ButtonEmbedManager manager) {
        this.manager = manager;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;

        String messageId = event.getMessageId();

        Optional<ButtonEmbed> embedOptional = manager.get(messageId);

        if (embedOptional.isEmpty()) return;

        ButtonEmbed embed = embedOptional.get();

        // reaction is getting removed no matter what
        String buttonId = event.getButton().getId();

        if (!embed.getActions().containsKey(buttonId)) return;

        embed.getActions().get(buttonId).apply(
                new ButtonEmbed.ClickContext(event.getMember(), event.getButton(),
                        event.getChannel().asTextChannel(),
                        event.getChannel().retrieveMessageById(event.getMessageId()), messageId))
                .queue(event, null); // null should be fine as event is never null
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        manager.remove(event.getMessageId());
    }
}
