package me.olliejonas.saltmarsh.embed;

import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record ItemizedEmbed<E extends Itemizable>(List<E> items, Supplier<EmbedBuilder> base, String title,
                                                  String author, int itemsPerPage, boolean pageCount,
                                                  boolean displayIndex, boolean asFields) implements DecoratedEmbed {

    public static final String AS_FIELD_SPLIT_STR = ">";

    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    public PaginatedEmbed compile(PaginatedEmbedManager manager) {
        PaginatedEmbed embed = compile();
        embed.compile(manager);
        return embed;
    }

    private PaginatedEmbed compile() {
        // compile items into representation
        AtomicInteger counter = new AtomicInteger(1);
        List<String> representations = items.stream()
                .map(Itemizable::representation)
                .map(i -> (displayIndex ? counter.getAndIncrement() + ". " : "") + i)
                .toList();

        Stream<EmbedBuilder> batches = MiscUtils.batches(representations, itemsPerPage).map(this::from);

        if (!title.equals(""))
            batches = batches.map(b -> b.setTitle(title));

        if (!author.equals(""))
            batches = batches.map(b -> b.setAuthor(author));

        List<EmbedBuilder> builders = batches.toList();

        if (pageCount) {
            int noPages = builders.size();
            counter.set(1);
            builders = builders.stream().map(b -> b.setFooter("Page " + counter.incrementAndGet() + " / " + noPages))
                    .toList();
        }

        return PaginatedEmbed.builder().embeds(builders).build();
    }

    private EmbedBuilder from(List<String> page) {
        EmbedBuilder builder = base.get();
        if (asFields) {
            page.forEach(str -> {
                List<String> split = Arrays.stream(str.split(AS_FIELD_SPLIT_STR)).map(String::strip).toList();
                if (split.size() != 2)
                    // would need to change this a bit if you wanted to make it so that users can make itemized embeds
                    // (could probably just get away with catching IllegalArgumentExceptions & handling errors)
                    throw new IllegalArgumentException("itemized embeds that are using fields can only have 1 \" - \"!");

                builder.addField(split.get(0).strip(), split.get(1).strip(), false);
            });
        } else {
            builder.setDescription(String.join("\n", page));
        }

        return builder;
    }

    public static <E extends Itemizable> Builder<E> builder() {
        return new Builder<>();
    }

    public static class Builder<E extends Itemizable> {

        private final List<E> items;

        private Supplier<EmbedBuilder> base;

        private String title;

        private String author;

        private int itemsPerPage;

        private boolean pageCount;

        private boolean displayIndex;

        private boolean asFields;

        Builder() {
            this.items = new ArrayList<>();
            this.base = EmbedUtils::colour;
            this.title = "";
            this.author = "";
            this.itemsPerPage = DEFAULT_ITEMS_PER_PAGE;

            this.pageCount = false;
            this.displayIndex = false;
            this.asFields = false;
        }

        public Builder<E> items(Collection<? extends E> items) {
            this.items.addAll(items);
            return this;
        }

        public Builder<E> item(E item) {
            this.items.add(item);
            return this;
        }

        public Builder<E> base(Supplier<EmbedBuilder> base) {
            this.base = base;
            return this;
        }

        public Builder<E> title(String title) {
            this.title = title;
            return this;
        }

        public Builder<E> author(String author) {
            this.author = author;
            return this;
        }

        public Builder<E> pageCount() {
            return pageCount(true);
        }

        public Builder<E> pageCount(boolean pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public Builder<E> itemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public Builder<E> displayIndex() {
            return displayIndex(true);
        }

        public Builder<E> displayIndex(boolean displayIndex) {
            this.displayIndex = displayIndex;
            return this;
        }

        public Builder<E> asFields() {
            return asFields(true);
        }

        public Builder<E> asFields(boolean asFields) {
            this.asFields = asFields;
            return this;
        }

        public ItemizedEmbed<E> build() {
            return new ItemizedEmbed<>(items, base, title, author, itemsPerPage, pageCount, displayIndex, asFields);
        }
    }
}
