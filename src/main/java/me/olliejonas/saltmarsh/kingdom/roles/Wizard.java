package me.olliejonas.saltmarsh.kingdom.roles;

import lombok.Setter;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.commands.NextRoundCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Map;

public class Wizard extends Role {

    private int winRoundCount;

    @Setter
    private float multiplier = 2.0F;

    public Wizard(KingdomGame game) {
        super(game);

        setColor(Color.MAGENTA);
    }

    @Override
    public MessageEmbed description() {
        this.winRoundCount = (int) Math.ceil(game.getRoleMap().size() * multiplier);
        return startingEmbed(String.format("""
                Survive %d rounds (starting with the King) to win!
                """, this.winRoundCount),
                String.format("""
                - This particular win round was calculated by taking the number of players (%d) multiplied by some multiplier (%02f) (rounded up).
                - For registering your win, you can either keep track of each round using Saltmarsh, by typing /next-round when it's the King's turn (you can choose whether people see that you typed it), or type in /win-condition after agreeing with everyone at the table.
                """, game.getRoleMap().size(), multiplier))
                .build();
    }

    @Override
    public boolean winConditions() {
        return game.isAlive(this) && game.getRound() <= winRoundCount;
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
