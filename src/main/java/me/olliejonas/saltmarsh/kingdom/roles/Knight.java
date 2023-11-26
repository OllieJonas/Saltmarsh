package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Map;

public class Knight extends Role {

    public Knight(KingdomGame game) {
        super(game);

        setColor(Color.GRAY);
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("""
                For the Honour of Greyskull!
                
                Survive with the King to win!
                """, """
                - You win by being the last one to survive with the King.
                - *You must immediately concede the game if the King dies for any reason.*
                """).addField("Challenger", """
                - There is a chance for someone in your game to be a Challenger. Their goal is to kill you and win with the King.
                - The game *cannot end* if both of you are still alive (regardless of other people's Win Conditions).
                
                """, false).addField("Tips & Tricks", """
                - You literally lose if you let the King die, so do everything in your power to protect them!
                
                """, false)
                .build();
    }

    @Override
    public boolean loseConditions() {
        return !game.isClassAlive(King.class) && game.getRoleMap().values().stream()
                .filter(role -> role instanceof Usurper).map(role -> (Usurper) role).noneMatch(game::isAlive);
    }

    @Override
    public boolean concedeConditions() {
        Map<Class<? extends Role>, Long> roles = game.getAliveRoleClasses();

        if (roles.containsKey(King.class)) return false;

        if (!game.getRoleClasses().containsValue(Usurper.class))  // if the game doesn't have a usurper & king
            return true;


        return !roles.containsKey(Usurper.class);  // if the usurper is alive, there is a chance of winning
    }

    @Override
    public boolean suppressOthersWinConditions() {
        return game.isAlive(this) && game.isClassAlive(Challenger.class);
    }


    @Override
    public boolean winConditions() {
        Map<Class<? extends Role>, Long> roles = game.getAliveRoleClasses();

        return roles.size() == 2 && game.isAlive(this) && (roles.containsKey(King.class) || roles.containsKey(Usurper.class));
    }
}
