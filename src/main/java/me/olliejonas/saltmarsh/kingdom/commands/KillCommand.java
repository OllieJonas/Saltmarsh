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
import java.util.Objects;

public class KillCommand extends Command {

    private final KingdomGameRegistry registry;

    public KillCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.ALL, "kill");
        this.registry = registry;
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.USER, "target", "the target which you (/ someone) killed!", true),
                new OptionData(OptionType.USER, "killer", "the person who did the killing! (defaults to you)", false)
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Member target = args.get("target").getAsMember();
        Member killer = executor;


        if (args.get("killer") != null)
            killer = args.get("killer").getAsMember();

        Objects.requireNonNull(killer);
        Objects.requireNonNull(target);

        if (!registry.inGame(killer))
            return InteractionResponses.error("The killer (" + killer.getAsMention() + ") must be in a game of Kingdom!");

        if (!registry.inGame(target))
            return InteractionResponses.error("The target (" + target.getAsMention() + ") must be in a game of Kingdom!");

        KingdomGame game = registry.getGame(target);

        if (game == null)
            throw new IllegalStateException("this should never happen (checked above)");


        game.kill(killer, target);

        registry.checks(game);

        return InteractionResponses.titleDescription(KingdomGame.NAME,
                killer.getEffectiveName() + " has killed " + target.getEffectiveName() + "!");
    }
}
