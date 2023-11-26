package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Map;

public class Challenger extends Role {

    public Challenger(KingdomGame game) {
        super(game);

        setColor(Color.DARK_GRAY);
        setRevealStrategy(RevealStrategy.builder()
                .role(Knight.class)
                .anonymous(true).build());
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("""
                Challenge the Knight in combat, for only one may stand!
                
                The win condition is to survive with the King (ensuring the Knight is dead).
                """, """
                - The Knight has been anonymously notified the fact there is a Challenger in the lobby.
                - If both of you and the Knight are still alive, then the game **cannot** end (no matter other people's win conditions).
                """).build();
    }

    @Override
    public boolean winConditions() {
        Map<Class<? extends Role>, Long> roles = game.getAliveRoleClasses();

        return roles.size() == 2 && game.isAlive(this) && (roles.containsKey(King.class) || roles.containsKey(Usurper.class));
    }

    @Override
    public boolean suppressOthersWinConditions() {
        return game.isAlive(this) && game.isClassAlive(Knight.class);
    }

}
