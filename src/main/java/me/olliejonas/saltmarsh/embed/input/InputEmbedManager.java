package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;
import me.olliejonas.saltmarsh.util.structures.WeakConcurrentHashMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jooq.lambda.tuple.Tuple3;

import java.util.HashSet;
import java.util.Set;

public class InputEmbedManager {

    private final WeakConcurrentHashMap<String, Tuple3<String, String, InputEmbed>> activeInputEmbedsAndChannels;

    // ids
    private final Set<String> usersExpectingInteraction;

    private final ButtonEmbedManager buttonEmbedManager;

    public InputEmbedManager(ButtonEmbedManager buttonEmbedManager) {
        this.buttonEmbedManager = buttonEmbedManager;
        this.activeInputEmbedsAndChannels = new WeakConcurrentHashMap<>();
        this.usersExpectingInteraction = new HashSet<>();
    }

    public boolean expectingInteraction(Member member) {
        return expectingInteraction(member.getId());
    }

    public boolean expectingInteraction(String userId) {
        return usersExpectingInteraction.contains(userId);
    }

    public void refreshExpectingInteraction(String userId, InputEmbed embed) {
        if (embed.expectingInteraction())
            usersExpectingInteraction.add(userId);
        else
            usersExpectingInteraction.remove(userId);
    }

    // returns whether the sender can actually send an InputEmbed (only allowed one active at a time)
    public boolean canSend(Member sender, InputEmbed embed) {


        refreshExpectingInteraction(sender.getId(), embed);
        return true;
    }

    public InteractionResponses createResponse(Member sender, TextChannel channel, InputEmbed embed) {
        if (activeInputEmbedsAndChannels.containsKey(sender.getId()))
            return InteractionResponses.error("You already have an active Wizard! Please finish that one before trying again!");

        refreshExpectingInteraction(sender.getId(), embed);
        return InteractionResponses.createData(embed.toCreateData(), false,
                        msg -> activeInputEmbedsAndChannels.put(sender.getId(), new Tuple3<>(channel.getId(),
                                msg.getId(), embed)));
    }

    public void remove(Member sender) {
        activeInputEmbedsAndChannels.remove(sender.getId());
    }

    public String getMessageId(Member sender) {
        return activeInputEmbedsAndChannels.get(sender.getId()).v2();
    }
    public InputEmbed getEmbed(Member sender) {
        return activeInputEmbedsAndChannels.get(sender.getId()).v3();
    }

    public boolean isInteractingWithEmbed(Member sender, TextChannel channel) {
        return activeInputEmbedsAndChannels.containsKey(sender.getId()) &&
                activeInputEmbedsAndChannels.get(sender.getId()).v1().equals(channel.getId());
    }

    public void destroy(String sender) {
        activeInputEmbedsAndChannels.remove(sender);
        usersExpectingInteraction.remove(sender);
    }
}
