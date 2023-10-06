package me.olliejonas.saltmarsh.embed.input.types;

import me.olliejonas.saltmarsh.embed.ButtonEmbed;
import me.olliejonas.saltmarsh.embed.input.InputButtonEmbed;

public class InputBooleanEmbed extends InputButtonEmbed<Boolean> {

    protected InputBooleanEmbed(ButtonEmbed embed) {
        super(embed, Boolean.class);
    }

}
