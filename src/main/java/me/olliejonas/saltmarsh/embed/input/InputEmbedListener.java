package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.input.types.InputCandidate;
import me.olliejonas.saltmarsh.embed.input.types.InputRepeatingText;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.tuple.Tuple3;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InputEmbedListener extends ListenerAdapter {

    private final InputEmbedManager manager;

    public InputEmbedListener(InputEmbedManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        String text = message.getContentRaw().strip();

        if (sender == null) return;
        if (!manager.isInteractingWithEmbed(sender, channel)) return;
        if (!manager.expectingInteraction(sender)) return;

        message.delete().queue();
        onInteraction(sender, channel, message, text, InputCandidate.Method.TEXT).queue(null, channel);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        String text = event.getButton().getLabel();

        if (sender == null) return;
        if (!manager.isInteractingWithEmbed(sender, channel)) return;

        if (text.equals("Exit")) {
            InputEmbed embed = manager.getEmbed(sender);
            message.delete().queue();
            manager.destroy(sender.getId());
            event.reply(MessageCreateData.fromEmbeds(embed.getExitPage())).setEphemeral(true).queue();
            return;
        }

        onInteraction(sender, channel, message, text, InputCandidate.Method.BUTTON).queue(event, channel);
    }

    private InteractionResponses onInteraction(Member sender, TextChannel channel, Message message, String text, InputCandidate.Method method) {
        InputEmbed embed = manager.getEmbed(sender);

        Tuple3<Optional<InputCandidate<?>>, Boolean, Boolean> success =
                embed.assignValueAndNext(sender, text, method);

        if (!success.v3())
            channel.sendMessageEmbeds(EmbedUtils.error("The thing you entered doesn't match the type expected! :( Please try typing something else that matches!")).queue();

        if (success.v1().isEmpty() || success.v2()) {
            message.editMessage(MessageEditData.fromCreateData(
                    new MessageCreateBuilder().setEmbeds(embed.getCompletionPage()).build())).queue(complete ->
                    complete.delete().queueAfter(10, TimeUnit.SECONDS));

            manager.destroy(sender.getId());
            return embed.onCompletion();
        }

        InputCandidate<?> candidate = success.v1().get();

        MessageCreateData next = (candidate instanceof InputRepeatingText repeating &&
                embed.getIdentifierToValueMap().containsKey(repeating.identifier())) ?
                repeating.buildEmbed((List<?>) embed.getIdentifierToValueMap().get(repeating.identifier())) :
                candidate.compile();

        MessageEditData nextEdit = MessageEditData.fromCreateData(next);

        queueEdit(sender, channel, message, nextEdit, method);

        return InteractionResponses.empty();
    }

    private void queueEdit(Member sender, TextChannel channel, Message eventMessage,
                           MessageEditData data, InputCandidate.Method method) {
        if (method == InputCandidate.Method.BUTTON)
            eventMessage.editMessage(data).queue();
        else
            channel.retrieveMessageById(manager.getMessageId(sender)).queue(message -> message.editMessage(data).queue());
    }
}
