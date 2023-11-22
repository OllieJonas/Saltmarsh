package me.olliejonas.saltmarsh.kingdom.roles;

import lombok.Getter;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

@Getter
public class King extends Role {

    private static final Random RANDOM = new Random();

    private static final Map<String, String> DEFAULT_CARD_MAP = Map.of(
            "Blind Obedience", "https://scryfall.com/card/gtc/6/blind-obedience"
    );

    private final Map<String, String> cardMap;

    private final String selectedCard;

    private final String selectedCardScryfall;

    public King(KingdomGame game) {
        this(game, DEFAULT_CARD_MAP);
    }

    public King(KingdomGame game, Map<String, String> cardMap) {
        super(game);
        this.cardMap = cardMap;

        Map.Entry<String, String> selectedCard = new ArrayList<>(cardMap.entrySet()).get(RANDOM.nextInt(cardMap.size()));

        this.selectedCard = selectedCard.getKey();
        this.selectedCardScryfall = selectedCard.getValue();

        setColor(Color.WHITE);
        setRevealStrategy(RevealStrategy.all(false));
    }

    @Override
    public MessageEmbed description() {
        return startingEmbed("""
                Rule your followers with an iron fist!
                
                To win, you must survive till you are the last one standing (excluding Knight or Challenger).
                """, """
                - This "last one standing" can also include either the Knight or the Challenger (but not both - one of them has to die!).
                - You will always start first (ask whoever won the dice roll to choose who gets to go first on XMage to select you).
                """).addField("Selected Card - " + this.selectedCard, String.format("""
                - You will also receive the card "%s" automatically in play from the first turn onwards (%s).
                - "Automatically in play from the first turn onwards" means that it starts in the command zone, similar to an Eminence effect".
        """, this.selectedCard, this.selectedCardScryfall), false).addField("Tips & Tricks", """
                - Both the Knight and the Challenger's win conditions are the same in reverse for you, but they are unable to win on their own!
                - This means they HAVE to protect you to win, but you don't have to protect them! Use this to your advantage ;).
                """, false)
                .build();


    }

    @Override
    public boolean winConditions() {
        Map<Class<? extends Role>, Long> roles = game.getAliveRoleClasses();
        int size = roles.size();


        return (size == 1 && roles.containsKey(King.class)) ||  // only King alive
                (size == 2 && roles.containsKey(King.class) &&  // King and Knight / Challenger alive
                        (roles.containsKey(Knight.class) || roles.containsKey(Challenger.class)));
    }
}
