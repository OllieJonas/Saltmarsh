package me.olliejonas.saltmarsh.embed.input;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.embed.input.types.InputTargetEmbed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class InputEmbed {
    private final List<InputTargetEmbed> embeds;

    private final int noPages;

    private final AtomicInteger currentPage;

    private final Map<String, Object> identifierToValueMap;

    private Function<Map<String, Object>, InteractionResponses> onCompletion;

    public InputEmbed(List<InputTargetEmbed> embeds) {
        this.embeds = embeds;

        this.noPages = embeds.size();
        this.currentPage = new AtomicInteger(0);

        this.identifierToValueMap = new HashMap<>();
    }

    public void compile(InputEmbedManager manager) {
        embeds.forEach(e -> e.compile(manager));
    }

    public InputTargetEmbed next() {
        return null;
    }

    public boolean assignValueAndNext(String value) {
        return true;
    }
}
