package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Jester extends Role {

    public Jester(KingdomGame game) {
        super(game);

        setColor(Color.RED);
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("""
                Chaos! Be the first person to die, or the last one standing! Simple as that!
                """).build();
    }

    @Override
    public boolean winConditions() {
        return (game.getAlivePlayers().size() == game.getRoleIdMap().size() - 1) && !game.isAlive(this);  // first person to die
    }
}
