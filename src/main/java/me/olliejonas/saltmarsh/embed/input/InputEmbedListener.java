package me.olliejonas.saltmarsh.embed.input;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class InputEmbedListener extends ListenerAdapter {

    private final InputEmbedManager manager;

    public InputEmbedListener(InputEmbedManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();

        if (sender == null) return;

        if (!manager.isInteractingWithEmbed(sender, channel)) return;

        InputEmbed embed = manager.get(sender);

        boolean success = embed.assignValueAndNext(event.getMessage().getContentStripped());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
    }
}
