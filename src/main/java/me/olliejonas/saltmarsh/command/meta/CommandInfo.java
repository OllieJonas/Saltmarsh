package me.olliejonas.saltmarsh.command.meta;

public record CommandInfo(String usage, String shortDesc, String longDesc) {
    public static CommandInfo empty() {
        return new CommandInfo("", "this cant be empty so here we are", "");
    }

    public static CommandInfo of(String shortDesc) {
        return new CommandInfo("", shortDesc, "");
    }

    public static CommandInfo of(String usage, String shortDesc, String longDesc) {
        return new CommandInfo(usage, shortDesc, longDesc);
    }

    public static CommandInfo of(String shortDesc, String longDesc) {
        return new CommandInfo("", shortDesc, longDesc);
    }
}
