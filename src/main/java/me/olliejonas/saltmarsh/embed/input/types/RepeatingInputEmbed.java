package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.input.InputEmbedManager;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.atomic.AtomicInteger;

public class RepeatingInputEmbed implements InputTargetEmbed {

    private final AtomicInteger skip = new AtomicInteger(0);

    public RepeatingInputEmbed(MessageEmbed embed) {

    }

    @Override
    public void compile(InputEmbedManager manager) {

    }

    @Override
    public int skip() {
        return skip.get();
    }
}
