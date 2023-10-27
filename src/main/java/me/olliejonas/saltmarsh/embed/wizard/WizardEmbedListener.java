package me.olliejonas.saltmarsh.embed.wizard;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.wizard.types.StepCandidate;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple4;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WizardEmbedListener extends ListenerAdapter {

    private final WizardEmbedManager manager;

    public WizardEmbedListener(WizardEmbedManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        String text = message.getContentRaw().strip();

        if (sender == null) return;
        if (manager.isNotInteracting(sender, channel) || !manager.requiresText(channel)) return;

        onInteraction(sender, channel, message, Collections.singletonList(text),
                StepCandidate.Method.TEXT, null).queue(null, channel);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        List<String> text = event.getValues();

        onInteraction(sender, channel, message, text,
                StepCandidate.Method.SELECT, event.getComponent()).queue(event, channel);
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        List<String> text = event.getValues().stream().map(IMentionable::getAsMention).toList();

        onInteraction(sender, channel, message, text,
                StepCandidate.Method.SELECT, event.getComponent()).queue(event, channel);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member sender = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        String text = event.getButton().getLabel();

        if (sender == null) return;
        if (manager.isNotInteracting(sender, channel)) return;

        if (text.equals("Exit")) {
            WizardEmbed embed = manager.getEmbed(channel);
            message.delete().queue();
            manager.destroy(channel);
            event.reply(MessageCreateData.fromEmbeds(embed.getExitPage())).setEphemeral(true).queue();
            return;
        }

        onInteraction(sender, channel, message, Collections.singletonList(text),
                StepCandidate.Method.BUTTON, event.getButton()).queue(event, channel);
    }

    private InteractionResponses onInteraction(Member sender, TextChannel channel, Message message, List<String> text,
                                               StepCandidate.Method method, @Nullable ActionComponent component) {
        WizardEmbed embed = manager.getEmbed(channel);

        Tuple4<Optional<StepCandidate<?>>, Boolean, Boolean, String> success =
                embed.assignValueAndNext(sender, text, method, component);

        if (!success.v3()) {
            System.out.println(success.v4());
            return InteractionResponses.error(success.v4(), false);
        }

        if (success.v1().isEmpty() || success.v2()) {
            MessageEditData editData = MessageEditData.fromCreateData(
                    new MessageCreateBuilder().setEmbeds(embed.getCompletionPage()).build());

            queueEdit(channel, message, editData, method, complete ->
                    complete.delete().queueAfter(10, TimeUnit.SECONDS));

            manager.destroy(channel);
            return embed.onCompletion();
        }

        StepCandidate<?> candidate = success.v1().get();
        MessageCreateData next = candidate.compile(embed.isShowExitButton());

        MessageEditData nextEdit = MessageEditData.fromCreateData(next);

        queueEdit(channel, message, nextEdit, method);

//        return InteractionResponses.messageAsEmbed("Successfully registered \"" + text.get(0) + "\" as input!", true);
        return InteractionResponses.empty();
    }

    private void queueEdit(TextChannel channel, Message eventMessage, MessageEditData data, StepCandidate.Method method) {
        queueEdit(channel, eventMessage, data, method, __ -> {});
    }

    private void queueEdit(TextChannel channel, Message eventMessage,
                           MessageEditData data, StepCandidate.Method method, Consumer<? super Message> onSuccess) {
        if (method == StepCandidate.Method.BUTTON || method == StepCandidate.Method.SELECT)
            eventMessage.editMessage(data).queue();
        else
            channel.retrieveMessageById(manager.getMessageId(channel)).queue(message -> message.editMessage(data).queue(onSuccess));
    }
}
