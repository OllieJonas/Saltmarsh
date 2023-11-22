package me.olliejonas.saltmarsh.command.meta;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CommandRegistry {

    Map<String, Command> getCommandMap();

    void register(Command command);

    default void register(Collection<Command> commands) {
        for (Command command : commands) {
            register(command);
        }
    }

    Optional<? extends Command> getByRoot(String name);

    Optional<? extends Command> getByClass(Class<? extends Command> command);

    Command getOrThrow(String name) throws CommandFailedException;

}
