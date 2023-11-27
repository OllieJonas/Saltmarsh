package me.olliejonas.saltmarsh.kingdom.roles;

import lombok.Setter;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.commands.NextRoundCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Map;

public class Wizard extends Role {

    private String winRoundCount;

    @Setter
    private float multiplier = 2.0F;

    public Wizard(KingdomGame game) {
        super(game);

        setColor(Color.MAGENTA);
    }

    @Override
    public MessageEmbed description() {
        // game is null in KingdomDescriptionCommand. SHOULD NOT BE NULL FOR ANY OTHER REASON !!
        this.winRoundCount = game == null ? "X" : String.valueOf((int) Math.ceil(game.getRoleMap().size() * multiplier));
        String noPlayers = game == null ? "-" : "(" + game.getRoleMap().size() + ")";

        return startingEmbed(String.format("""
                _"We love casting spells" - Joeyy_
                
                Survive %s rounds (starting with the King) to win!
                """, this.winRoundCount),
                String.format("""
                - This particular win round was calculated by taking the number of players %s multiplied by some multiplier (%.2f) (rounded up).
                - For registering your win, you can either keep track of each round using Saltmarsh, by typing `/next-round` when it's the King's turn (you can choose whether people see that you typed it), or reveal your role with `/reveal-role` in chat and agreeing with the table that you've won!
                """, noPlayers, multiplier))
                .build();
    }

    @Override
    public boolean winConditions() {
        return game.isAlive(this) && game.getRound() <= Integer.parseInt(winRoundCount);
    }

    @Override
    public Map<Class<? extends Command>, String> usefulCommands() {
        return defaultRequiredCommands()
                .add(NextRoundCommand.class, "To keep track of how many rounds have passed with Saltmarsh, use this!")
                .build();
    }

    @Override
    public int minimumRequiredPlayers() {
        return 7;
    }
}
