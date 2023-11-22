package me.olliejonas.saltmarsh.kingdom;

import lombok.Getter;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandRegistry;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.kingdom.roles.Role;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KingdomGameRegistry {

    private final CommandRegistry commandRegistry;

    @Getter
    private final Map<Member, KingdomGame> gamesMap;

    private final Map<KingdomGame, Map<Member, RevealedMessage>> revealedRolesMessageMap;

    // true if ended, false if not
    public boolean endGameContaining(Member target, boolean forced) {
        KingdomGame game = getGame(target);

        updateRevealedMessages(game);

        if (game != null) endGame(game, forced);


        return game != null;
    }

    record RevealedMessage(Message message, Role role, Map<Class<? extends Role>, Integer> revealedCounts) {
    };

    public KingdomGameRegistry(@Nullable CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        this.gamesMap = new HashMap<>();
        this.revealedRolesMessageMap = new HashMap<>();
    }

    public KingdomGame startGame(Collection<Member> members, TextChannel channel, RoleAllocation.Strategy strategy) {
        KingdomGame game = new KingdomGame(channel, strategy);

        for (Member member : members) {
            if (gamesMap.containsKey(member))
                throw new IllegalStateException(member.getEffectiveName() + " is already a part of a game!");

            gamesMap.put(member, game);
        }

        String error = game.start(members);

        if (error != null && !error.isEmpty()) {
            channel.sendMessageEmbeds(EmbedUtils.error(error)).queue();
            return null;
        }

        announceGameStart(game);

        return game;
    }

    private void announceGameStart(KingdomGame game) {
        Map<? extends Member, ? extends Role> validDmTargets = validDmTargets(game);

        validDmTargets.forEach((member, role) -> member.getUser().openPrivateChannel().flatMap(privateChannel -> {
            EmbedBuilder message = new EmbedBuilder(role.description());

            if (commandRegistry != null) {
                String usefulCommandsStr = role.usefulCommands().entrySet().stream()
                        .map(entry -> new Tuple2<>(
                                commandRegistry.getByClass(entry.getKey())
                                        .map(Command::getMetadata)
                                        .map(Command.Metadata::primaryAlias)
                                        .orElseThrow(), entry.getValue()))
                        .map(entry -> "`/" + entry.v1() + "` - " + entry.v2())
                        .collect(Collectors.joining("\n"));

                message.addField("Useful Commands", usefulCommandsStr, false).build();
            }

            return privateChannel.sendMessageEmbeds(message.build());
        }).queue());

        revealedRolesMessageMap.put(game, new HashMap<>());

        buildRevealedRoleEmbeds(game, validDmTargets).forEach(tuple -> tuple.v1().getUser().openPrivateChannel()
                        .flatMap(channel -> channel.sendMessageEmbeds(tuple.v3()))
                        .queue(message -> revealedRolesMessageMap.get(game).put(tuple.v1(),
                                        new RevealedMessage(message, validDmTargets.get(tuple.v1()), tuple.v2()))));
    }

    private Map<? extends Member, ? extends Role> validDmTargets(KingdomGame game) {
        return game.getRoleMap().entrySet().stream()
                .filter(entry -> !entry.getKey().getUser().isBot())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * why on God's green earth does this exist ...
     * <p>
     * this transforms a map of members into a list of messages to be sent / edited to each member regarding which roles
     * are personally revealed to them.
     * <p>
     * it's needed in both the initial derivation of who's revealed, and any subsequent reconstructions.
     *
     * @param dmTargets
     * - 1st is for this member
     * - 2nd is for revealed counts - i.e. how many of each role are shown to the player (useful for Bandit with only revealing one)
     * - 3rd is who is actually revealed (member to role)
     *
     * @return see desc
     */
    private Stream<Tuple3<? extends Member, Map<Class<? extends Role>, Integer>, MessageEmbed>> buildRevealedRoleEmbeds(
            KingdomGame game, Map<? extends Member, ? extends Role> dmTargets) {

        return dmTargets.entrySet().stream()
                .map(entry -> new Tuple1<>(entry.getKey()).concat(getRequiredRevealRoles(game, entry.getValue().getClass())))
                .filter(tuple -> !tuple.v3().isEmpty())
                .map(tuple -> tuple.map3(map -> EmbedUtils.colour(
                "Revealed Roles",
                "As part of your role, I can reveal the following roles to you:\n\n" +
                        map.entrySet().stream()
                                .map(entry -> "- " + (entry.getValue().revealStrategy().anonymous() ? "There" : entry.getKey().getAsMention()) +
                                        " is a " + entry.getValue().name())
                                .collect(Collectors.joining("\n")))));
    }

    private Tuple2<Map<Class<? extends Role>, Integer>, Map<Member, Role>> getRequiredRevealRoles(KingdomGame game, Class<? extends Role> role) {
        Map<Class<? extends Role>, Integer> revealedCounts = game.getRoleClasses()
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toMap(r -> r, __ -> 0, (r1, __) -> r1));

        return new Tuple2<>(revealedCounts, game.getRoleMap().entrySet().stream()
                .peek(entry -> revealedCounts.put(entry.getValue().getClass(), revealedCounts.get(entry.getValue().getClass()) + 1))
                .filter(entry -> entry.getValue().revealStrategy(game, (__, ___) -> {}).shouldReveal(role, revealedCounts.get(role) - 1))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public boolean inGame(Member member) {
        return gamesMap.containsKey(member);
    }

    public KingdomGame getGame(Member member) {
        return gamesMap.get(member);
    }

    public boolean hasAnActiveGame(Guild guild) {
        Collection<Member> guildMembers = new HashSet<>(guild.getMembers());

        return gamesMap.keySet().stream().anyMatch(guildMembers::contains);
    }

    public boolean checkForEnd(KingdomGame game) {
        boolean ended = game.isEnded();

        if (ended)
            endGame(game);

        return ended;
    }

    public void updateRevealedMessages(KingdomGame game) {
        Map<Member, Role> roles = revealedRolesMessageMap.get(game).entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().role()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        buildRevealedRoleEmbeds(game, roles)
                .map(tuple3 -> tuple3.concat(revealedRolesMessageMap.get(game).get(tuple3.v1()).message()))
                .forEach(tuple4 -> tuple4.v4().editMessage(MessageEditData.fromEmbeds(tuple4.v3())).queue());
    }

    public void endGame(KingdomGame game) {
        endGame(game, false);
    }

    public void endGame(KingdomGame game, boolean forced) {
        Set<Member> members = game.getRoleMap().keySet();

        if (forced)
            game.getTextChannel().sendMessageEmbeds(EmbedUtils.colour("Kingdom",
                    "The game for " + members.stream()
                            .map(Member::getEffectiveName)
                            .collect(Collectors.joining(", ")) + " has been forcibly ended!")).queue();

        for (Member member : game.getRoleMap().keySet()) {
            if (!inGame(member))
                continue;

            gamesMap.remove(member);
        }

    }
}
