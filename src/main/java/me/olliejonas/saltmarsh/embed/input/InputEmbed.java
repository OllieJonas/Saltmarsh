package me.olliejonas.saltmarsh.embed.input;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InputEmbed {
    private final List<InputTargetEmbed> embeds;

    private int noPages;

    private AtomicInteger currentPage;

    public InputEmbed(List<InputTargetEmbed> embeds) {
        this.embeds = embeds;

        this.noPages = embeds.size();
        this.currentPage = new AtomicInteger(0);
    }

    public InputTargetEmbed next() {
        return null;
    }

}
