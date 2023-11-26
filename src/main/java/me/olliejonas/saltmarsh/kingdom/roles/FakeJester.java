package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class FakeJester extends King {

    public FakeJester(KingdomGame game) {
        super(game);

        setColor(Color.MAGENTA);
    }

    @Override
    public String name() {
        return "Jester (Fake King)";
    }

    @Override
    public String displayName() {
        return "King";
    }

    @Override
    public MessageEmbed description() {
        return new EmbedBuilder()
                .setTitle(KingdomGame.NAME)
                .setDescription("You are a Jester (disguised as a King! >:) )")
                .addField("Description", """
                        Oh boy this is going to be fun ...
                        
                        Whenever a Kingdom game is started, there is a small chance (10%) that **everyone** in the game is a Jester. This is one of those games.
                        
                        To make it seem like a normal game for everyone else, you have been publicly "outed" as the King.
                        You have also been given a card to start with, like the King would start with, and will be chosen to go first.
                        
                        You are NOT the King, but don't let them find out!
                        """, false)
                .addField("Win Conditions (Jester)", "Be the first person to die. Simple.", false)
                .addField("Win Conditions (King) - Just for ✨ roleplay ✨", "Be the last one standing (with or without the Knight or Challenger)", false)
                .addField(selectedCardField())
                .addField("Tips & Tricks", """
                        - You are at a significant advantage with the information you have, so don't try to play too meanly! <3
                        - That being said, you can leverage the information you have here to your advantage.
                        """, false)
                .build();
    }

    @Override
    public boolean winConditions() {
        return (game.getAlivePlayers().size() == game.getRoleMap().size() - 1) && !game.isAlive(this);
    }
}
