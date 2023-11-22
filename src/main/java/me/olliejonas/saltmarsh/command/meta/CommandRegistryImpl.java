package me.olliejonas.saltmarsh.command.meta;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class CommandRegistryImpl implements CommandRegistry {

    private final Map<String, Command> commandMap;

    private final Map<Class<? extends Command>, Command> classToCommandMap;


    public CommandRegistryImpl() {
        this.commandMap = new HashMap<>();
        this.classToCommandMap = new HashMap<>();
    }

    @Override
    public void register(Command command) {
        command.addSubCommands();

        commandMap.put(command.getMetadata().primaryAlias(), command);
        command.getMetadata().aliases().forEach(als -> commandMap.put(als, command));

        classToCommandMap.put(command.getClass(), command);
    }

    @Override
    public Optional<Command> getByRoot(String command) {
        return Optional.ofNullable(commandMap.get(command));
    }

    @Override
    public Optional<? extends Command> getByClass(Class<? extends Command> command) {
        return Optional.ofNullable(classToCommandMap.get(command));
    }

    public Command getOrThrow(String command) throws CommandFailedException {
        return getByRoot(command).orElseThrow(() ->
                CommandFailedException.unrecognisedCommand(command,
                        String.format("Unrecognised command %s", command)));
    }
}
