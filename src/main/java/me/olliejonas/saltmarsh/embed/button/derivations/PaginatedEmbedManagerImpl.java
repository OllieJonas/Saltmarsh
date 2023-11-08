package me.olliejonas.saltmarsh.embed.button.derivations;

import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.button.ButtonEmbedManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PaginatedEmbedManagerImpl implements PaginatedEmbedManager {

    private final ButtonEmbedManager buttonEmbedManager;
    private final Map<String, PaginatedEmbed> paginatedEmbedMap;


    public PaginatedEmbedManagerImpl(ButtonEmbedManager buttonEmbedManager) {
        this.buttonEmbedManager = buttonEmbedManager;
        this.paginatedEmbedMap = new HashMap<>();
    }

    public InteractionResponses register(PaginatedEmbed paginatedEmbed,
                                         Function<PaginatedEmbed, Optional<ButtonEmbed>> initialEmbedFunction, Runnable onFailure) {
        compile(paginatedEmbed);

        ButtonEmbed first = initialEmbedFunction.apply(paginatedEmbed).orElse(null);

        if (first == null)
            onFailure.run();

        return buttonEmbedManager.register(first, message -> paginatedEmbedMap.put(message.getId(), paginatedEmbed));
    }

    public Optional<PaginatedEmbed> get(String messageId) {
        return Optional.ofNullable(paginatedEmbedMap.get(messageId));
    }

    private void compile(PaginatedEmbed embed) {
        BiFunction<ButtonEmbed.ClickContext, Function<PaginatedEmbed, Optional<ButtonEmbed>>, InteractionResponses> consumer = (context, function) -> {
            function.apply(embed).ifPresent(newEmbed -> get(context.messageId()).ifPresent(__ ->
                    context.message().queue(message -> message.editMessageEmbeds(newEmbed).queue())));

            return InteractionResponses.empty();
        };

        AtomicInteger counter = new AtomicInteger(0);

        embed.setCompiledPages(embed.getPages().stream().map(builder -> {
            builder.setFooter("Page (" + counter.incrementAndGet() + " / " + embed.getPages().size() + ")");
            ButtonEmbed.Builder buttonBuilder = ButtonEmbed.builder(builder);

            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(0), context -> consumer.apply(context, PaginatedEmbed::first));
            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(1), context -> consumer.apply(context, PaginatedEmbed::prev));
            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(2), context -> consumer.apply(context, PaginatedEmbed::next));
            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(3), context -> consumer.apply(context, PaginatedEmbed::last));

            return buttonBuilder.build();
        }).collect(Collectors.toList()));
    }

    public void remove(String messageId) {
        paginatedEmbedMap.remove(messageId);
    }
}
