package me.olliejonas.saltmarsh.command.meta;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
public class CommandFailedException extends RuntimeException {



    enum Reason {
        SALTMARSH_INTERNAL,
        JDA_INTERNAL,
        NO_PERMISSION,
        BAD_ARGUMENTS,
        UNRECOGNISED_COMMAND,
        OTHER
    }
    private final Reason reason;


    // sent to users
    private final String message;

    // more detailed config log
    private final String context;

    private final boolean responseEphemeral;

    public static CommandFailedException error(Member executor, String errorMessage) {
        return new CommandFailedException(Reason.OTHER, errorMessage, errorMessage);
    }

    public static CommandFailedException saltmashInternal(String context) {
        return new CommandFailedException(Reason.SALTMARSH_INTERNAL, "An internal error has occurred! (Saltmarsh Internal)", context);
    }

    public static CommandFailedException jdaInternal(String context) {
        return new CommandFailedException(Reason.JDA_INTERNAL, "An internal error has occured! (JDA Internal)", context);
    }

    public static CommandFailedException noPermission(Member executor, Command command) {
        return new CommandFailedException(Reason.NO_PERMISSION,
                "You don't have permission for this!",
                String.format("%s tried executing %s without permissions (No Permissions)",
                        executor.getNickname(), command.getPrimaryAlias()), true);
    }

    public static CommandFailedException badArgs(Member executor, Command command, String desc) {
        return new CommandFailedException(Reason.BAD_ARGUMENTS,
                String.format("Invalid arguments! Format: %s", desc),
                String.format("%s tried executing %s with bad arguments (Bad Arguments)",
                        executor.getNickname(), command.getPrimaryAlias()));
    }

    public static CommandFailedException unrecognisedCommand(String input, String context) {
        return new CommandFailedException(Reason.UNRECOGNISED_COMMAND,
                String.format("Sorry, but we were unable to find the command %s! " +
                        "Please refer to /saltmarsh-help for more information...", input), context);
    }

    public static CommandFailedException other(String message, String context) {
        return new CommandFailedException(Reason.OTHER, message, context);
    }

    public CommandFailedException(Reason reason, String message, String context) {
        this(reason, message, context, false);
    }

    public CommandFailedException(Reason reason, String message, String context, boolean responseEphemeral) {
        super(context);

        this.reason = reason;
        this.message = message;
        this.context = context;
        this.responseEphemeral = responseEphemeral;
    }
}
