package me.olliejonas.saltmarsh.music.exceptions;

import net.dv8tion.jda.api.entities.Member;

public class NoVoiceChannelException extends Exception {

    public NoVoiceChannelException() {
        this("I can't find a channel to join! :(");
    }

    public NoVoiceChannelException(Member member) {
        this(member.getEffectiveName());
    }

    public NoVoiceChannelException(String message) {
        super(message);
    }
}
