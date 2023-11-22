package me.olliejonas.saltmarsh.kingdom.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.KingdomGameRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public class RevealRolesCommand extends Command {

    private final KingdomGameRegistry registry;

    public RevealRolesCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.ADMIN, "reveal-roles");

        this.registry = registry;
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Guild guild = executor.getGuild();
        KingdomGame game = registry.getGame(executor);

        if (game == null) {
            Map.Entry<Member, KingdomGame> potentialGame = registry.getGamesMap().entrySet().stream()
                    .filter(entry -> guild.getMembers().contains(entry.getKey()))
                    .findAny().orElse(null);

            game = potentialGame != null ? potentialGame.getValue() : null;
        }

        if (game == null) {
            return InteractionResponses.error("I was unable to find a game on this server!");
        }

        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("WARNING (KINGDOM)")
                .setDescription(executor.getAsMention() + " has revealed all the roles in your game for themselves! Cheaty cheaty! :(")
                .setColor(Color.RED)
                .build()).queue();

        return new InteractionResponses.Embed(EmbedUtils.colour("Kingdom Roles",
                game.getRoleMap().entrySet().stream()
                        .map(e -> e.getKey().getEffectiveName() + " - " + e.getValue().name())
                        .collect(Collectors.joining("\n"))), true);
    }
}
