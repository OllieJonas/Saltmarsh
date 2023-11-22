package me.olliejonas.saltmarsh.kingdom.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.KingdomGameRegistry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ConcedeCommand extends Command {

    private final KingdomGameRegistry registry;

    public ConcedeCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.ALL, "concede");
        this.registry = registry;
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        KingdomGame game = registry.getGame(executor);

        if (game == null)
            return InteractionResponses.error("You aren't currently in a game of Kingdom!", true);

        game.concede(executor);
        registry.checks(game);

        return InteractionResponses.messageAsEmbed(executor.getEffectiveName() + " has conceded their game of Kingdom!");
    }
}
