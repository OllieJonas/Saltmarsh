package me.olliejonas.saltmarsh.music.exceptions;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import lombok.Getter;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;

@Getter
public class QueueException extends RuntimeException {

    public enum Reason {
        TRACK_ALREADY_QUEUED("This track has already been queued!"),
        TRACK_LOAD_FAILED("This track failed to load!"),

        NO_MATCHES("We were unable to find any matches for your search!"),
        EMPTY_QUEUE("The queue is currently empty!");

        @Getter
        private final String message;

        Reason(String message) {
            this.message = message;
        }
    }

    private final Reason reason;

    private final FriendlyException exception;

    public QueueException(Reason reason) {
        this(reason, null);
    }

    public QueueException(Reason reason, FriendlyException exception) {
        super(reason.getMessage());
        this.reason = reason;
        this.exception = exception;
    }

    public CommandFailedException asCommandFailed() {
        return CommandFailedException.other(reason.getMessage(), reason.getMessage());
    }
}
