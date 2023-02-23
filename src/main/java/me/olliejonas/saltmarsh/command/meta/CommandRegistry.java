package me.olliejonas.saltmarsh.command.meta;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {

    @Getter
    private final Map<String, Command> commandMap;

    public CommandRegistry() {
        this.commandMap = new HashMap<>();
    }

    public void register(Command command) {
        command.addSubCommands();
        commandMap.put(command.getPrimaryAlias(), command);
        command.getAliases().forEach(als -> commandMap.put(als, command));
    }

    public Optional<Command> getRoot(String command) {
        Command root = commandMap.get(command);

        return Optional.ofNullable(commandMap.get(command));
    }

    public Command getOrThrow(String command) throws CommandFailedException {
        return getRoot(command).orElseThrow(() ->
                CommandFailedException.unrecognisedCommand(command,
                        String.format("Unrecognised command %s", command)));
    }
}
