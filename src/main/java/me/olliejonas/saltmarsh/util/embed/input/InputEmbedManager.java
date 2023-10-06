package me.olliejonas.saltmarsh.util.embed.input;

import me.olliejonas.saltmarsh.util.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Map;

public class InputEmbedManager {

    private final WeakConcurrentHashMap<String, InputEmbed> activeInputEmbeds;

    private final ButtonEmbedManager buttonEmbedManager;

    public InputEmbedManager(ButtonEmbedManager buttonEmbedManager) {
        this.buttonEmbedManager = buttonEmbedManager;
        this.activeInputEmbeds = new WeakConcurrentHashMap<>();
    }

    public void send(TextChannel channel, InputEmbed embed) {

    }
}
