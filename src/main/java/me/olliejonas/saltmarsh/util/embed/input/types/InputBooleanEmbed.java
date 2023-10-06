package me.olliejonas.saltmarsh.util.embed.input.types;

import me.olliejonas.saltmarsh.util.embed.ButtonEmbed;
import me.olliejonas.saltmarsh.util.embed.input.InputButtonEmbed;

public class InputBooleanEmbed extends InputButtonEmbed<Boolean> {

    protected InputBooleanEmbed(ButtonEmbed embed) {
        super(embed, Boolean.class);
    }

}
