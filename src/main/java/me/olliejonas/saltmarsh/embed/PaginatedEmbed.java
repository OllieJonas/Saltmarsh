package me.olliejonas.saltmarsh.embed;

import lombok.*;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.util.functional.TriFunction;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class PaginatedEmbed implements DecoratedEmbed {

    private String title;

    private String footer;

    private String author;

    private CharSequence description;

    private Color colour;

    private String thumbnail;

    private String image;

    private TemporalAccessor timestamp;

    @Singular
    private List<EmbedBuilder> pages;

    private List<ButtonEmbed> compiledPages = new ArrayList<>();

    private boolean isCompiled = false;

    private AtomicInteger currentPage = new AtomicInteger();

    private int noPages;

    private PaginatedEmbed() {
    }

    public Optional<ButtonEmbed> curr() {
        return compiledPages.isEmpty() ? Optional.empty() : Optional.of(compiledPages.get(currentPage.get()));
    }

    public Optional<ButtonEmbed> prev() {
        if (currentPage.get() <= 0) return curr();

        return Optional.of(compiledPages.get(currentPage.decrementAndGet()));
    }

    public Optional<ButtonEmbed> next() {
        if (currentPage.get() + 1 == compiledPages.size()) return curr();

        return Optional.of(compiledPages.get(currentPage.incrementAndGet()));
    }

    public Optional<ButtonEmbed> first() {
        currentPage.set(0);
        return compiledPages.isEmpty() ? Optional.empty() : Optional.of(compiledPages.get(currentPage.get()));
    }

    public Optional<ButtonEmbed> last() {
        currentPage.set(pages.size() - 1);
        return compiledPages.isEmpty() ? Optional.empty() : Optional.of(compiledPages.get(currentPage.get()));
    }

    public void compile(PaginatedEmbedManager manager) {
        TriFunction<ButtonEmbed.ClickContext, String, Function<PaginatedEmbed, Optional<ButtonEmbed>>, InteractionResponses> consumer = (context, actionStr, function) -> {
            function.apply(this).ifPresent(newEmbed -> manager.get(context.messageId())
                    .ifPresent(embed -> context.message()
                            .queue(message -> message.editMessageEmbeds(newEmbed).queue())));

            return InteractionResponses.empty();
        };

        AtomicInteger counter = new AtomicInteger(0);
        this.compiledPages = pages.stream().map(builder -> {
            builder.setFooter("Page (" + counter.incrementAndGet() + " / " + pages.size() + ")");
            ButtonEmbed.Builder buttonBuilder = ButtonEmbed.builder(builder);

            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(0), context -> consumer.apply(context, "first", PaginatedEmbed::first));
            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(1), context -> consumer.apply(context, "previous", PaginatedEmbed::prev));
            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(2), context -> consumer.apply(context, "next", PaginatedEmbed::next));
            buttonBuilder.button(Constants.PAGINATED_EMBED_BUTTONS.get(3), context -> consumer.apply(context, "last", PaginatedEmbed::last));

            return buttonBuilder.build();
        }).collect(Collectors.toList());
    }

    /* BUILDER METHODS */


    public static Builder builder() {
        return new Builder();
    }
    public static Builder standard() {
        Builder builder = builder();
        builder.author(Constants.APP_TITLE);
        builder.colour(Constants.APP_COLOURS.get(1));
        builder.footer(EmbedUtils.footer());
        return builder;
    }

    public static Builder essentials() {
        Builder builder = builder();
        builder.author(Constants.APP_TITLE);
        builder.footer(EmbedUtils.footer());
        return builder;
    }


    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {

        private String author;

        private String title;

        private Color colour;

        private CharSequence description;

        private String footer;

        private String thumbnail;

        private String image;

        private TemporalAccessor timestamp;

        private final List<EmbedBuilder> embeds;

        public int noPages;

        public Builder() {
            this.embeds = new ArrayList<>();
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder colour(Color colour) {
            this.colour = colour;
            return this;
        }

        public Builder description(CharSequence description) {
            this.description = description;
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        public Builder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder timestamp(TemporalAccessor timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder page(EmbedBuilder builder) {
            return embed(builder);
        }

        public Builder embed(EmbedBuilder builder) {
            this.embeds.add(builder);
            return this;
        }

        public Builder embeds(EmbedBuilder builder, EmbedBuilder... builders) {
            return embed(builder).embeds(List.of(builders));
        }

        public Builder embeds(Collection<EmbedBuilder> builders) {
            embeds.addAll(builders);
            return this;
        }

        public Builder textPage(String text) {
            EmbedBuilder builder = EmbedUtils.standard();
            builder.setDescription(text);
            page(builder);
            return this;
        }

        public Builder textPage(String title, String text) {
            EmbedBuilder builder = EmbedUtils.standard();
            builder.setTitle(title);
            builder.setDescription(text);
            page(builder);
            return this;
        }

        public PaginatedEmbed build() {
            noPages = embeds.size();
            return new PaginatedEmbed(
                    title,
                    footer,
                    author,
                    description,
                    colour,
                    thumbnail,
                    image,
                    timestamp,
                    embeds,
                    new ArrayList<>(),
                    false,
                    new AtomicInteger(0),
                    noPages
            );
        }
    }
}
