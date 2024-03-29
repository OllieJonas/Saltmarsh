package me.olliejonas.saltmarsh.kingdom;

import lombok.Getter;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.kingdom.roles.Role;
import me.olliejonas.saltmarsh.util.BiMap;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class KingdomGame {

    public static final String NAME = "Kingdom";

    private final String name;

    // this doesn't change
    private final BiMap<Member, Role> roleMap;

    private final Map<Class<? extends Role>, Role> classRoleMap;

    // this changes with alive players
    private final Collection<Member> alivePlayers;

    private final RoleAllocation.Strategy roleAllocationStrategy;

    private final Guild guild;

    private final TextChannel textChannel;

    private final AtomicReference<Message> globallyRevealedMessage;

    private final Set<Member> alreadySentLossOrConcede;

    private int round;

    private boolean ended;

    public KingdomGame(TextChannel channel) {
        this(NAME, channel, new RoleAllocation.Default());
    }

    public KingdomGame(TextChannel channel, RoleAllocation.Strategy roleAllocationStrategy) {
        this(NAME, channel, roleAllocationStrategy);
    }

    public KingdomGame(String name, TextChannel channel, RoleAllocation.Strategy roleAllocationStrategy) {
        Objects.requireNonNull(channel);

        this.name = name;

        this.textChannel = channel;
        this.guild = textChannel.getGuild();

        this.roleAllocationStrategy = roleAllocationStrategy;

        this.roleMap = new BiMap<>();
        this.classRoleMap = new HashMap<>();

        this.alreadySentLossOrConcede = new HashSet<>();

        this.alivePlayers = new HashSet<>();
        this.round = 1;
        this.ended = false;

        this.globallyRevealedMessage = new AtomicReference<>();
    }

    public String start(Collection<Member> members) {
        alivePlayers.addAll(members);
        return announceAndAllocateRoles(members);
    }

    public void end() {
        ended = true;
    }

    public int incrementRound() {
        roleMap.values().forEach(role -> role.onNextRound(++round));

        checkGameStatus(true);
        updateRevealedMessages();
        return round;
    }

    public boolean concede(Member target) {
        return kill(null, target);
    }

    public boolean kill(Member killer, Member target) {
        alivePlayers.remove(target);

        if (killer != null) {
            roleMap.get(killer).onKill(target);
            roleMap.get(target).onSelfDeath(killer);
        }

        boolean gameStatus = checkGameStatus();
        updateRevealedMessages();

        return gameStatus;
    }

    public Collection<Member> getMembersWithRole(Class<? extends Role> role) {
        return roleMap.entrySet().stream()
                .filter(entry -> role.equals(entry.getValue().getClass()))
                .map(Map.Entry::getKey).toList();
    }

    private String announceAndAllocateRoles(Collection<Member> members) {
        // remove saltmarsh from members (in case it's joined for music)
        members.removeIf(member -> member.getId().equals(Constants.SELF_USER_ID));

        Tuple2<Map<? extends Member, ? extends Role>, String> roleAllocation = roleAllocationStrategy.allocate(this, members);

        if (roleAllocation.v1() == null || (roleAllocation.v2() != null && !roleAllocation.v2().isEmpty()))
            return roleAllocation.v2();

        this.roleMap.putAll(roleAllocation.v1().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        this.classRoleMap.putAll(roleAllocation.v1().values().stream()
                .collect(Collectors.toMap(role -> role.getClass(), role -> role,
                (BinaryOperator<Role>) (role, role2) -> role)));

        textChannel.sendMessageEmbeds(buildGloballyRevealedRolesEmbed()).queue(this.globallyRevealedMessage::set);
        return null;
    }

    public Map<Member, Role> getRoleMap() {
        return roleMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Member, Class<? extends Role>> getRoleClasses() {
        return roleMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getClass()));
    }

    public Map<Class<? extends Role>, Long> getAliveRoleClasses() {
        return alivePlayers.stream().map(roleMap::get).collect(Collectors.groupingBy(Role::getClass, Collectors.counting()));
    }

    public boolean isAlive(Role role) {
        return getAliveRoleClasses().containsKey(role.getClass());
    }

    public <T extends Role> T getRoleByClass(Class<T> role) {
        return (T) classRoleMap.get(role);
    }

    private MessageEmbed buildGloballyRevealedRolesEmbed() {
        BiFunction<Member, Role, BiConsumer<Role.RevealStrategy, Role.RevealStrategy>> onRoleChange =
                (member, role) -> (__, latest) -> textChannel.sendMessageEmbeds(EmbedUtils.colour(NAME, latest.onRevealMessage().apply(member, role))).queue();

        EmbedBuilder builder = EmbedUtils.colour().setTitle(name);

        BiFunction<String, Boolean, String> crossText = (original, cross) -> (cross ? "~~" : "") + original + (cross ? "~~" : "");

        roleMap.entrySet().stream().map(entry -> {
            boolean dead = !alivePlayers.contains(entry.getKey());

            return new MessageEmbed.Field(crossText.apply(entry.getKey().getEffectiveName(), dead),
                    crossText.apply("Role: " + (ended || entry.getValue()
                            .revealStrategy(this, onRoleChange.apply(entry.getKey(), entry.getValue())).revealGloballyPublicly()
                            ? entry.getValue().displayName() : "Not known!"), dead),
                true);}).forEach(builder::addField);


        Map<Role, Long> anonymousRoles = roleMap.values().stream()
                .filter(role -> role.revealStrategy().revealGlobally() && role.revealStrategy().anonymous())
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        String description = anonymousRoles.isEmpty() ? "" : "The following roles are in your lobby (either dead or alive): " +
                anonymousRoles.entrySet().stream()
                        .map(entry -> entry.getValue() + " " + entry.getKey().displayName() + (entry.getValue() != 1 ? "s" : ""))
                        .collect(Collectors.joining(", "));

        if (ended) description = "The game is over! Here are all the roles for each player";

        builder.setDescription(description);

        return builder.build();
    }

    public void updateRevealedMessages() {
        CompletableFuture.completedFuture(globallyRevealedMessage.get())
                .thenAccept(message -> message.editMessage(MessageEditData.fromEmbeds(buildGloballyRevealedRolesEmbed())).submit())
                .whenComplete((__, e) -> {if (e != null) e.printStackTrace();});
    }

    public Role getRole(Member player) {
        return roleMap.get(player);
    }

    // 1st elem is whether someone has won
    // 2nd is those required to concede
    private boolean checkGameStatus() {
        return checkGameStatus(false);
    }

    // check for concedes is performed in KingdomGameRegistry to force them to concede in Saltmarsh
    private boolean checkGameStatus(boolean winsOnly) {
        if (!winsOnly) {
            checkForLosses();
        }

        return checkForWins();
    }

    private void checkForLosses() {
        checkForLossesOrConcedes(Role::loseConditions,
                "Unfortunately, you can no longer meet your win condition." +
                        "You don't have to concede or announce this, but just to let you know you can't win... rip :(",
                null, () -> {});
    }

    public Collection<Member> checkForConcedes() {
        return checkForLossesOrConcedes(role -> role.concedeConditions() && isAlive(role),
                "Due to certain conditions in the game, you are required to concede the game!",
                members -> members.stream()
                        .map(Member::getEffectiveName)
                        .collect(MiscUtils.joinWithAnd())
                        + " ha" + (members.size() == 1 ? "s" : "ve") + " to concede the game! big sad for them", () -> {});
    }

    private Collection<Member> checkForLossesOrConcedes(Predicate<Role> condition, String privateMessage,
                                          Function<Collection<Member>, String> globalMessage, Runnable allMatch) {

        Set<Member> members = getRoleMap().entrySet().stream()
                .filter(entry -> condition.test(entry.getValue()))
                .filter(entry -> !alreadySentLossOrConcede.contains(entry.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        alreadySentLossOrConcede.addAll(members);

        members.forEach(member -> member.getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(EmbedUtils.from(name, privateMessage)))
                .queue());

        if (globalMessage != null && !members.isEmpty())
            textChannel.sendMessageEmbeds(EmbedUtils.from(name, globalMessage.apply(members))).queue();

        return members;
    }

    private boolean checkForWins() {
        boolean suppressOthersWinConditions = roleMap.values().stream().anyMatch(Role::suppressOthersWinConditions);

        Map<Member, Role> winners = roleMap.entrySet().stream()
                .filter(entry -> entry.getValue().winConditions())
                .filter(entry -> !suppressOthersWinConditions || entry.getValue().overrideSuppressionOfWinConditions())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        MessageEmbed winnerEmbed = buildWinnerEmbeds(winners);

        if (winnerEmbed != null) {
            textChannel.sendMessageEmbeds(winnerEmbed).queue();
            end();
        }

        return winnerEmbed != null;
    }

    private MessageEmbed buildWinnerEmbeds(Map<Member, Role> winners) {
        if (winners.isEmpty()) return null;

        String winnersText = winners.entrySet().stream().map(e -> e.getKey().getAsMention() + " (" + e.getValue().name() + ")").collect(MiscUtils.joinWithAnd());

        return EmbedUtils.colour(name, winnersText + " ha" + (winners.size() == 1 ? "s" : "ve") + " won the game!");
    }

//    private Stream<Map.Entry<Member, Role>> membersFromIds(Map<String, Role> memberIds) {
//        Map<String, CompletableFuture<Member>> idToMemberFutures = memberIds.keySet().stream()
//                .collect(Collectors.toMap(id -> id, this::memberFromIdAsync));
//
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(idToMemberFutures.values().toArray(new CompletableFuture[0]));
//
//        try {
//            allOf.get();
//        } catch (ExecutionException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        return idToMemberFutures.entrySet().stream().map(e -> {
//            try {
//                return Map.entry(e.getValue().get(), memberIds.get(e.getKey()));
//            } catch (InterruptedException | ExecutionException ex) {
//                throw new RuntimeException(ex);
//            }
//        });
//    }

    private Stream<Map.Entry<String, Role>> idsFromMembers(Map<Member, Role> members) {
        return members.entrySet().stream().map(entry -> Map.entry(entry.getKey().getId(), entry.getValue()));
    }

    private CompletableFuture<Member> memberFromIdAsync(String id) {
        Member member = guild.getMemberById(id);

        if (member != null)
            return CompletableFuture.completedFuture(member);

        return guild.retrieveMemberById(id).submit();
    }

    private CompletableFuture<Message> messageFromIdAsync(String id) {
        Message message = textChannel.getHistory().getMessageById(id);

        if (message != null)
            return CompletableFuture.completedFuture(message);

        return textChannel.retrieveMessageById(id).submit();
    }


    public boolean isClassAlive(Class<? extends Role> role) {
        return getAliveRoleClasses().containsKey(role);
    }
}
