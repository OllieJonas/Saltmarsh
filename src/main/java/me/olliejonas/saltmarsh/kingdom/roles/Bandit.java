package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Bandit extends Role {

    public Bandit(KingdomGame game) {
        super(game);

        setColor(Color.BLACK);
        setRevealStrategy(game.getRoleIdMap().size() >= 6 ?
                RevealStrategy.builder().role(Bandit.class).amount(1).build() : RevealStrategy.NONE);
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("Kill the King with your fellow comrades to win!", """
                - There are, by default, __always 2 Bandits__ for any game.
                - There may be up to **3** bandits in games with 6 player or above games.
                - If there are more than 6 players, you are able to see the identity of up to **one** other bandit per game (there may be 2 other Bandits, but you will __only__ see one.).
                """, """
                - Only one of you actually needs to survive for all of you to win!
                """).build();
    }

    // if the king is dead, and there is at least 1 bandit
    @Override
    public boolean winConditions() {
        return !game.getAliveRoleClasses().containsKey(King.class) && game.getAliveRoleClasses().containsKey(Bandit.class);
    }

    @Override
    public int minimumRequiredPlayers() {
        return 6;
    }
}
