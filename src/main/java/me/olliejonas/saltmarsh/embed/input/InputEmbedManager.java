package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.embed.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jooq.lambda.tuple.Tuple3;

public class InputEmbedManager {

    private final WeakConcurrentHashMap<String, Tuple3<String, String, InputEmbed>> activeInputEmbedsAndChannels;

    private final ButtonEmbedManager buttonEmbedManager;

    public InputEmbedManager(ButtonEmbedManager buttonEmbedManager) {
        this.buttonEmbedManager = buttonEmbedManager;
        this.activeInputEmbedsAndChannels = new WeakConcurrentHashMap<>();
    }

    public void send(Member sender, TextChannel channel, InputEmbed embed) {
        activeInputEmbedsAndChannels.put(sender.getId(), new Tuple3<>(channel.getId(), "", embed));
    }

    public void remove(Member sender) {
        activeInputEmbedsAndChannels.remove(sender.getId());
    }

    public InputEmbed get(Member sender) {
        return activeInputEmbedsAndChannels.get(sender.getId()).v3();
    }

    public boolean isInteractingWithEmbed(Member sender, TextChannel channel) {
        return activeInputEmbedsAndChannels.containsKey(sender.getId()) &&
                activeInputEmbedsAndChannels.get(sender.getId()).v1().equals(channel.getId());
    }
}
