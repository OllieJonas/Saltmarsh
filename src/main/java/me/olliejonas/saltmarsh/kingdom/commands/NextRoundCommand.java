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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NextRoundCommand extends Command {

    private final KingdomGameRegistry registry;

    public NextRoundCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.ALL, "next-round");

        this.registry = registry;
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.BOOLEAN, "broadcast-round", "whether to announce the next round to everyone (defaults to true)")
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        if (!registry.inGame(executor))
            return InteractionResponses.error("You must be in a game of Kingdom to use this command!");

        KingdomGame game = registry.getGame(executor);
        int round = game.incrementRound();

        registry.checkForEnd(game);
        registry.updateRevealedMessages(game);

        boolean ephemeral = false;

        if (args.containsKey("broadcast-round"))
            ephemeral = args.get("broadcast-round").getAsBoolean();


        return InteractionResponses.messageAsEmbed("You are now on round " + round + "!", !ephemeral);
    }
}
