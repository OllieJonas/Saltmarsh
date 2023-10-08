package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.types.InputCandidate;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.tuple.Tuple3;

import java.util.Optional;

public class InputEmbedListener extends ListenerAdapter {

    private final InputEmbedManager manager;

    public InputEmbedListener(InputEmbedManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        String text = event.getMessage().getContentRaw().strip();

        if (sender == null) return;
        if (!manager.isInteractingWithEmbed(sender, channel)) return;
        if (!manager.expectingInteraction(sender)) return;

        onInteraction(sender, channel, text, InputCandidate.Method.TEXT).queue(null, channel);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        String text = event.getButton().getLabel();

        if (sender == null) return;
        if (!manager.isInteractingWithEmbed(sender, channel)) return;

        onInteraction(sender, channel, text, InputCandidate.Method.BUTTON).queue(event, channel);
    }

    private InteractionResponses onInteraction(Member sender, TextChannel channel, String text, InputCandidate.Method method) {

        InputEmbed embed = manager.getEmbed(sender);

        Tuple3<Optional<InputCandidate<?>>, Boolean, Boolean> success =
                embed.assignValueAndNext(sender, text, method);

        if (!success.v3())
            channel.sendMessageEmbeds(EmbedUtils.error("The thing you entered doesn't match the type expected! :( Please try typing something else that matches!")).queue();

        if (success.v1().isEmpty() || success.v2()) {
            channel.retrieveMessageById(manager.getMessageId(sender)).queue((message -> {
                message.editMessage(MessageEditData.fromCreateData(
                        new MessageCreateBuilder().setEmbeds(embed.getCompletionPage()).build())).queue();
            }));
            manager.destroy(sender.getId());
            return embed.onCompletion();
        }

        MessageCreateData next = success.v1().get().compile();

        MessageEditData nextEdit = MessageEditData.fromCreateData(next);

        channel.retrieveMessageById(manager.getMessageId(sender)).queue((message -> {
            message.editMessage(nextEdit).queue();
        }));

        return InteractionResponses.empty();
    }
}
