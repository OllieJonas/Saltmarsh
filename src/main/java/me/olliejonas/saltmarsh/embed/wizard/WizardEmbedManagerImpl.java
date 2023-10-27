package me.olliejonas.saltmarsh.embed.wizard;

import me.olliejonas.saltmarsh.InteractionResponses;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WizardEmbedManagerImpl implements WizardEmbedManager {


    private final Map<String, Context> activeInputEmbedsAndChannels;

    public WizardEmbedManagerImpl() {
        this.activeInputEmbedsAndChannels = new HashMap<>();
    }

    public InteractionResponses register(@Nullable Member sender, TextChannel channel,
                                         WizardEmbed embed) {
        if (activeInputEmbedsAndChannels.containsKey(channel.getId()))
            return InteractionResponses.error("There is already an active Wizard in this channel! " +
                    "Please finish that one before trying again!");

        return InteractionResponses.createData(embed.toCreateData(), false,
                        msg -> activeInputEmbedsAndChannels.put(channel.getId(),
                                new Context(sender == null ? "any" : sender.getId(), msg.getId(), embed)));
    }

    public boolean isNotInteracting(Member sender, TextChannel channel) {
        return !activeInputEmbedsAndChannels.containsKey(channel.getId()) ||
                (!activeInputEmbedsAndChannels.get(channel.getId()).sender().equals("any") &&
                        !activeInputEmbedsAndChannels.get(channel.getId()).sender().equals(sender.getId()));
    }

    public boolean requiresText(TextChannel channel) {
        return activeInputEmbedsAndChannels.containsKey(channel.getId()) &&
                activeInputEmbedsAndChannels.get(channel.getId()).embed().curr().requiresText();
    }

    public void destroy(TextChannel channel) {
        activeInputEmbedsAndChannels.remove(channel.getId());
    }

    public Context getContext(TextChannel channel) {
        return activeInputEmbedsAndChannels.get(channel.getId());
    }
}
