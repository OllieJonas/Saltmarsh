package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

public class InputEmbedManager {

    // key is text channel, value is member id, message id and inputembed
    private final WeakConcurrentHashMap<String, Tuple3<String, String, InputEmbed>> activeInputEmbedsAndChannels;

    private final ButtonEmbedManager buttonEmbedManager;

    public InputEmbedManager(ButtonEmbedManager buttonEmbedManager) {
        this.buttonEmbedManager = buttonEmbedManager;
        this.activeInputEmbedsAndChannels = new WeakConcurrentHashMap<>();
    }

    public Tuple2<InteractionResponses, Boolean> createEmbed(@Nullable Member sender, TextChannel channel,
                                                             InputEmbed embed) {
        if (activeInputEmbedsAndChannels.containsKey(channel.getId()))
            return new Tuple2<>(InteractionResponses.error("There is already an active Wizard in this channel! " +
                    "Please finish that one before trying again!"), false);

        return new Tuple2<>(InteractionResponses.createData(embed.toCreateData(), false,
                        msg -> activeInputEmbedsAndChannels.put(channel.getId(),
                                new Tuple3<>(sender == null ? "any" : sender.getId(), msg.getId(), embed))), true);
    }

    public void remove(Member sender) {
        activeInputEmbedsAndChannels.remove(sender.getId());
    }

    public String getMessageId(TextChannel channel) {
        return activeInputEmbedsAndChannels.get(channel.getId()).v2();
    }
    public InputEmbed getEmbed(TextChannel channel) {
        return activeInputEmbedsAndChannels.get(channel.getId()).v3();
    }

    public boolean isInteractingWithEmbed(Member sender, TextChannel channel) {
        return activeInputEmbedsAndChannels.containsKey(channel.getId()) &&
                (activeInputEmbedsAndChannels.get(channel.getId()).v1().equals("any") ||
                        activeInputEmbedsAndChannels.get(channel.getId()).v1().equals(sender.getId()));
    }

    public void destroy(TextChannel channel) {
        activeInputEmbedsAndChannels.remove(channel.getId());
    }
}
