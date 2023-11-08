package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class AmIConnectedToADatabaseCommand extends Command {


    private final boolean amIConnected;

    public AmIConnectedToADatabaseCommand(boolean amIConnected) {
        super(CommandPermissions.ADMIN, "am-i-connected-to-a-database");
        this.amIConnected = amIConnected;
    }
    
    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.messageAsEmbed("I am " + (amIConnected ? "" : "not ") + "connected to a database!");
    }
}
