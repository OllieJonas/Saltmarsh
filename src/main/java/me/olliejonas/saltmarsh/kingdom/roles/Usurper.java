package me.olliejonas.saltmarsh.kingdom.roles;

import lombok.Getter;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jooq.lambda.tuple.Tuple2;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

/**
 * Aim of Usurper is to kill the King, then survive as the King. If they kill the King.
 * <p>
 * Reveal Conditions - Upon killing the King
 * Win Conditions - Last alive (can be with Challenger)
 * Lose Conditions - Not killing the King
 */
public class Usurper extends Role {

    @Getter
    private boolean transformed = false;

    private boolean isKingDead = false;


    public Usurper(KingdomGame game) {
        super(game);

        setColor(Color.BLUE);
        setRevealStrategy(RevealStrategy.builder()
                .transformation(__ -> new Tuple2<>(transformed, Collections.singleton(Role.class)))
                .onRevealMessage((member, role) -> "As a Usurper, " + member.getEffectiveName() +
                        " has become the new reigning Monarch! Long reign the new King!")
                .anonymous(false).build());
    }

    @Override
    public String name() {
        return "Usurper";
    }

    @Override
    public String displayName() {
        return transformed ? "~~_" + name() + "_~~" + " King" : getClass().getSimpleName();
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("""
                Death to the King!
                
                Your job is to kill the King, then you become the new one! Then you need to survive as the new one!
                """, """
                - You lose if you're not the one to kill (deal the final blow) the King (you don't concede).
                - Upon killing the King, you will be announced globally as the new King.
                - Once killing the current King is achieved, you will be globally announced as the new King.
                - Once this has occurred, you will then have to fulfill the King's win condition to win: __be the last one standing__.
                - You will NOT get the King's starting card for yourself.
                """).build();
    }

    @Override
    public boolean winConditions() {
        Map<Class<? extends Role>, Long> roles = game.getAliveRoleClasses();
        int size = roles.size();

        return transformed && (size == 1 && roles.containsKey(Usurper.class)) ||  // only Usurper alive
                (size == 2 && roles.containsKey(Usurper.class) &&  // Usurper and Knight / Challenger alive
                (roles.containsKey(Knight.class) || roles.containsKey(Challenger.class)));
    }

    @Override
    public void onDeath(Member killer, Member target) {
        if (game.getRoleMap().get(target) instanceof King)
            isKingDead = true;
    }

    @Override
    public boolean loseConditions() {
        return isKingDead && !transformed;  // if the king is dead, and they haven't transformed, they can't win
    }

    @Override
    public void onKill(Member target) {
        if (game.getRoleMap().get(target) instanceof King) {
            transformed = true;
        }
    }
}
