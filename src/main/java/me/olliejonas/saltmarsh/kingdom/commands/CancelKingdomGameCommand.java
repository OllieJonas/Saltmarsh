package me.olliejonas.saltmarsh.kingdom.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
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

public class CancelKingdomGameCommand extends Command {


    private final KingdomGameRegistry registry;

    public CancelKingdomGameCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.EVENTS, "cancel-kingdom");

        this.registry = registry;
    }


    @Override
    public CommandInfo info() {
        return CommandInfo.of("(KINGDOM) Cancels the game that user is in!");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.USER, "user", "the user to cancel the game for! (defaults to yourself")
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Member target = executor;

        if (args.containsKey("user"))
            target = args.get("user").getAsMember();

        return target != null && registry.endGameContaining(target, true) ?
                InteractionResponses.messageAsEmbed("Successfully ended game for " + target.getEffectiveName()) :
                InteractionResponses.error("This person isn't in a game!");
    }
}
