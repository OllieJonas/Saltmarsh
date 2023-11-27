package me.olliejonas.saltmarsh.kingdom.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.KingdomGameRegistry;
import me.olliejonas.saltmarsh.kingdom.roles.Role;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RevealRoleCommand extends Command {

    private final KingdomGameRegistry registry;

    public RevealRoleCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.ALL, "reveal-role");

        this.registry = registry;
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("(KINGDOM) Reveal your role to everyone!");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
          new OptionData(OptionType.USER, "target", "Someone else you want to reveal (ADMIN ONLY)")
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Member target = executor;

        if (args.containsKey("target") && CommandPermissions.ADMIN.hasPermission(executor))
            target = args.get("target").getAsMember();

        if (target == null)
            return InteractionResponses.error("target is null! (shouldn't see this, contact Ollie)");

        KingdomGame game = registry.getGame(target);

        if (game == null)
            return InteractionResponses.error("You're not currently in a game!");

        Role role = game.getRole(target);

        return InteractionResponses.messageAsEmbed(target.getAsMention() + " is a " + role.name() + "!");
    }
}
