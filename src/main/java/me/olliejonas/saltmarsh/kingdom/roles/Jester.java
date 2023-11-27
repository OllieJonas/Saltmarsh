package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Jester extends Role {

    private boolean hasWon;

    public Jester(KingdomGame game) {
        super(game);

        this.hasWon = false;

        setColor(Color.RED);
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("""
                Chaos!
                
                Be the first person to die, or the last one standing! Simple as that!
                """, """
                - This win condition will override anything that suppresses win conditions (for example, the Knight vs Challenger).
                """).build();
    }

    @Override
    public boolean overrideSuppressionOfWinConditions() {
        return true;
    }

    @Override
    public boolean winConditions() {
        // this extra variable is to ensure the Jester wins after win conditions have stopped being suppressed.
        if (!hasWon)
            this.hasWon = (game.getAlivePlayers().size() == game.getRoleMap().size() - 1) && !game.isAlive(this);

        return hasWon;
    }
}
