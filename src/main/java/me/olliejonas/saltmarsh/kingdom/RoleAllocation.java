package me.olliejonas.saltmarsh.kingdom;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.kingdom.roles.*;
import me.olliejonas.saltmarsh.util.structures.WeightedRandomSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface RoleAllocation {

    int MAX_XMAGE_SIZE = 10;

    static String checkMaxPlayers(Collection<Member> members) {
        return members.size() > MAX_XMAGE_SIZE ? "You can't have more than 10 people!" : null;
    }

    interface Strategy {

        // the extra string is an error message
        Tuple2<Map<? extends Member, ? extends Role>, String> allocate(KingdomGame game, Collection<Member> members);

        Tuple2<MessageEmbed, FileUpload> announcement(KingdomGame game);
    }

    static Strategy factory(String name) {
        return switch(name) {
            default -> new Default();
        };
    }

    class Default implements Strategy {

        static Function<KingdomGame, List<Role>> DEFAULT_ROLES = game -> List.of(
                new King(game),
                new Knight(game),
                new Bandit(game),
                new Bandit(game)
        );

        static BiFunction<KingdomGame, Integer, WeightedRandomSet<Role>> OTHER_ROLES = (game, size) -> {

            WeightedRandomSet<Role> set = new WeightedRandomSet<>();

            // 5+ players
            set.add(new Jester(game));
            set.add(new Usurper(game));
            set.add(new Challenger(game));

            // 6+ players
            set.add(new Bandit(game), 0.5);

            // 7+ players
            set.add(new Wizard(game));

            set.removeIf(role -> role.minimumRequiredPlayers() > size);

            return set;
        };

        @Override
        public Tuple2<Map<? extends Member, ? extends Role>, String> allocate(KingdomGame game, Collection<Member> members) {
            String error = checkMaxPlayers(members);

            if (error != null) {
                return new Tuple2<>(null, error);
            }

            List<Member> membersList = new ArrayList<>(members);

            int membersSize = membersList.size();

            List<Role> defaultRoles = DEFAULT_ROLES.apply(game);
            WeightedRandomSet<Role> otherRoles = OTHER_ROLES.apply(game, membersSize);

            // guaranteed 4 defaults + 1 extra (5 total)
            if (membersSize < defaultRoles.size() + 1)
                return new Tuple2<>(null, "Requires " + (defaultRoles.size() + 1) + " members to start a default game!");

            List<Role> roles = new ArrayList<>(defaultRoles);

            for (int i = defaultRoles.size(); i < membersSize; i++) {
                Role toAdd = otherRoles.getRandom(false);

                if (toAdd instanceof Bandit) otherRoles.add(toAdd);  // don't replace bandits

                roles.add(toAdd);
            }

            // only really need to shuffle one of these, but why not do both?
            Collections.shuffle(membersList);
            Collections.shuffle(roles);

            return new Tuple2<>(Seq.seq(membersList).zip(Seq.seq(roles)).collect(Collectors.toMap(Tuple2::v1, Tuple2::v2)), null);
        }

        @Override
        public Tuple2<MessageEmbed, FileUpload> announcement(KingdomGame game) {
            King king = game.getRoleByClass(King.class);
            Member kingMember = game.getMembersWithRole(King.class).stream().findFirst().orElseThrow();

            boolean hasStartingCards = !king.getCardMap().isEmpty();

            EmbedBuilder builder = EmbedUtils.colour().setTitle(game.getName())
                    .setDescription("Successfully started Kingdom game with " + game.getRoleIdMap().size() + "! " +
                            "Everyone, please check your DMs for your role!")
                    .addField("King", "The King is " + kingMember.getAsMention(), true);

            if (hasStartingCards)
                builder.addField("Starting Card", king.getSelectedCard() + " (" + king.getSelectedCardScryfall() + ")", true);

            builder.addField("Rules & How to Play", """
                            
                """, false);

            builder.addField("Setup Game", String.format("""                            
                1) Setup a new match as normal.
                2) Click on "Custom Options", tick "Emblem Cards", and put the file attached to this message in the "Starting Player File" option.
                3) Make sure that when everyone's rolled to see who goes first, the winner selects the King (that's %s) to start the game.
                
                """, kingMember.getAsMention()), false);

            if (hasStartingCards)
                builder.setFooter("The possible starting cards for the King could have been: " +
                        king.getCardMap().entrySet().stream()
                                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                                .collect(Collectors.joining(", ")));


            FileUpload attachment = generateStartingFile(king.getSelectedCard(), king.getSelectedCard());
            return new Tuple2<>(builder.build(), null);

        }

        private FileUpload generateStartingFile(String nameSimplified, String nameFull) {
            return FileUpload.fromStreamSupplier(nameSimplified + ".dck", () -> new ByteArrayInputStream(("1 " + nameFull).getBytes()));
        }
    }

    record Determined(Map<Member, Class<? extends Role>> roles) implements Strategy {

        public static Determined single(Member member, String role) {
            return single(member, RoleFactory.factory(role));
        }

        public static Determined single(Member member, Class<? extends Role> role) {
            return new Determined(Map.of(member, role));
        }

        @Override
        public Tuple2<Map<? extends Member, ? extends Role>, String> allocate(KingdomGame game, Collection<Member> members) {
            if (!members.containsAll(roles.keySet()) || !roles.keySet().containsAll(members))
                return new Tuple2<>(null, "Factory members and allocate members must be identical!");

            return new Tuple2<>(roles.entrySet().stream()
                    .map(e -> new Tuple2<>(e.getKey(), RoleFactory.factory(e.getValue(), game)))
                    .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2)), "");
        }

        @Override
        public Tuple2<MessageEmbed, FileUpload> announcement(KingdomGame game) {
            return new Tuple2<>(EmbedUtils.colour()
                    .setTitle("Kingdom")
                    .setDescription("You are currently running a custom Kingdom game containing the following roles:\n" +
                            game.getRoleIdMap().values().stream()
                                    .map(role -> "- " + role.name()).sorted()
                                    .collect(Collectors.joining("\n")))
                    .addField("!! WARNING !!", "Interactions between Roles are only tested & supported for the " +
                            "default role allocation (2 Bandits, 1 Knight, 1 King)! Choosing roles that deviate from this may result in unintended behaviour!", false)
                    .build(),
                    null);
        }
    }
}
