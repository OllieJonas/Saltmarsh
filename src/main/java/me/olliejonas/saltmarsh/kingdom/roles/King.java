package me.olliejonas.saltmarsh.kingdom.roles;

import lombok.Getter;
import lombok.Setter;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.util.structures.WeightedRandomSet;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class King extends Role {

    public static final ScryfallCard MONARCH_CARD = ScryfallCard.of("Crown of Gondor", "https://scryfall.com/card/ltc/75/crown-of-gondor");

    public static final ScryfallCard NO_HAND_SIZE_CARD = ScryfallCard.of("Thought Vessel", "https://scryfall.com/card/cmm/414/thought-vessel");

    private static final WeightedRandomSet<StartingCard> DEFAULT_POSSIBLE_STARTING_CARDS = new WeightedRandomSet<>();

    static {
        DEFAULT_POSSIBLE_STARTING_CARDS.add(StartingCard.of("Blind Obedience", "https://scryfall.com/card/gtc/6/blind-obedience"));
        DEFAULT_POSSIBLE_STARTING_CARDS.add(StartingCard.of("Always Watching", "https://scryfall.com/card/gnt/7/always-watching"));
        DEFAULT_POSSIBLE_STARTING_CARDS.add(StartingCard.of("Ever-Watching Threshold", "https://scryfall.com/card/c18/9/ever-watching-threshold"));
        DEFAULT_POSSIBLE_STARTING_CARDS.add(StartingCard.of("Bloodsworn Steward", "https://scryfall.com/card/voc/144/bloodsworn-steward"));
    }

    public record ScryfallCard(String name, String scryfall) {
        public static ScryfallCard of(String name, String scryfall) {
            return new ScryfallCard(name, scryfall);
        }


        public String xMageDeckRepresentation() {
            String code = getCode();
            return "1 " + code + " " + name();
        }

        public String getCode() {
            String[] split = scryfall.split("/");
            String set = split[4];
            String no = split[5];
            return "[" + set + ":" + no + "]";
        }
    }
    public record StartingCard(ScryfallCard card, boolean noMaxHandSize, boolean startAsMonarch) {

        public static StartingCard of(String name, String scryfall) {
            return new StartingCard(ScryfallCard.of(name, scryfall), false, true);
        }

        public String representation() {
            return card.name() + " (" + card.scryfall() + ")";
        }

        public String toDeckList() {
            List<ScryfallCard> cards = new ArrayList<>();
            cards.add(card);

            if (startAsMonarch)
                cards.add(MONARCH_CARD);

            if (noMaxHandSize)
                cards.add(NO_HAND_SIZE_CARD);

            return cards.stream()
                    .map(ScryfallCard::xMageDeckRepresentation).collect(Collectors.joining("\n")) + "\n" +
                    "LAYOUT MAIN:(1,1)(NONE,false,50)|" + cards.stream().map(ScryfallCard::getCode).collect(Collectors.joining(",", "(", ")")) + "\n" +
                    "LAYOUT SIDEBOARD:(0,0)(NONE,false,50)|";
        }
    }

    private final WeightedRandomSet<StartingCard> possibleStartingCards;

    @Setter
    protected StartingCard startingCard;

    public King(KingdomGame game) {
        this(game, DEFAULT_POSSIBLE_STARTING_CARDS);
    }

    public King(KingdomGame game, WeightedRandomSet<StartingCard> possibleStartingCards) {
        super(game);
        this.possibleStartingCards = possibleStartingCards;
        this.startingCard = possibleStartingCards.getRandom();

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
                """)
                .addField("Monarch", String.format("You will always start with the card %s. " +
                        "This allows you to become the monarch upon first playing a legendary creature.", MONARCH_CARD.name()), false)
                .addField(selectedCardField())
                .addField("Tips & Tricks", """
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

    protected MessageEmbed.Field selectedCardField() {
        String card = startingCard == null ? "X" : startingCard.card().name();
        String scryfall = startingCard == null ? "X" : startingCard.card().scryfall();
        boolean noHandSize = startingCard != null && startingCard.noMaxHandSize();

        String thoughtVessel = "- You will also start with the card Thought Vessel (i.e. you have no maximum hand size)";

        return new MessageEmbed.Field("Selected Card - " + card,
                String.format("- You will also receive the card %s automatically in play from the first turn onwards (%s).\n", card, scryfall) +
                        (noHandSize ? thoughtVessel : "") +
                        "- \"Automatically in play from the first turn onwards\" means that it starts in the command zone, similar to an Eminence effect.", false);
    }
}
