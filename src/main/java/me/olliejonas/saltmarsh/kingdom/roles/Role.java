package me.olliejonas.saltmarsh.kingdom.roles;

import lombok.Builder;
import lombok.Setter;
import lombok.Singular;
import me.olliejonas.saltmarsh.Constants;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.commands.ConcedeCommand;
import me.olliejonas.saltmarsh.kingdom.commands.KillCommand;
import me.olliejonas.saltmarsh.util.FluentMapBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jooq.lambda.tuple.Tuple2;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Role {

    // tuple in transformation: 1st boolean dictates whether it should transform, second boolean denotes what to.
    // can only do one transformation in a given game.
    // BiFunction - member can be null if anonymous
    @Builder
    public record RevealStrategy(@Singular Collection<Class<? extends Role>> roles,
                                 Function<KingdomGame, Tuple2<Boolean, Collection<Class<? extends Role>>>> transformation,
                                 BiFunction<Member, Role, String> onRevealMessage,
                                 boolean anonymous, int amount) {

        public RevealStrategy {
            if (roles == null) roles = Collections.emptyList();

            if (transformation == null) {
                Collection<Class<? extends Role>> finalRoles = roles;
                transformation = __ -> new Tuple2<>(false, finalRoles);
            }

            if (onRevealMessage == null) {
                onRevealMessage = (member, role) -> (anonymous ? "Someone" : member.getEffectiveName()) +
                        " has been revealed as a " + role.name() + " in your game!";
            }

            if (amount == 0) amount = -1;
        }

        static final RevealStrategy NONE = RevealStrategy.builder().roles(Collections.emptyList()).build();

        static RevealStrategy all(boolean anonymous) {
            return RevealStrategy.builder().roles(Collections.singleton(Role.class)).anonymous(anonymous).build();
        }

        public RevealStrategy transform(KingdomGame game) {
            Tuple2<Boolean, Collection<Class<? extends Role>>> transformed = transformation.apply(game);

            return transformed.v1() ?
                    new RevealStrategy(transformed.v2(), __ -> new Tuple2<>(false, transformed.v2()),
                            onRevealMessage, anonymous, amount) :
                    this;
        }

        public boolean shouldReveal(Class<? extends Role> role, int amountRevealed) {
            return roles().contains(role) && (amountRevealed < amount || amount == -1);
        }

        public boolean revealGlobally() {
            return roles.contains(Role.class);
        }

        public boolean revealGloballyPublicly() {
            return revealGlobally() && !anonymous;
        }
    }

    protected final KingdomGame game;

    protected RevealStrategy revealStrategy;

    @Setter
    protected Color color;

    public Role(KingdomGame game) {
        this.game = game;

        this.color = Constants.APP_COLOUR;
        this.revealStrategy = RevealStrategy.NONE;
    }

    public String name() {
        return getClass().getSimpleName();
    }

    public String displayName() {
        return name();
    }

    public abstract MessageEmbed description();

    public void setRevealStrategy(RevealStrategy strategy) {
        this.revealStrategy = strategy;
    }

    public RevealStrategy revealStrategy() {
        return revealStrategy;
    }

    public RevealStrategy revealStrategy(KingdomGame game) {
        return revealStrategy(game, (__, ___) -> {});
    }

    public RevealStrategy revealStrategy(KingdomGame game, BiConsumer<RevealStrategy, RevealStrategy> onChange) {
        RevealStrategy newRevealStrategy = revealStrategy.transform(game);
        boolean changed = this.revealStrategy != newRevealStrategy;

        if (changed)
            onChange.accept(this.revealStrategy, newRevealStrategy);

        this.revealStrategy = newRevealStrategy;

        return revealStrategy;
    }

    // key is a command class, value is a description
    public Map<Class<? extends Command>, String> usefulCommands() { return defaultRequiredCommands().build(); };

    protected FluentMapBuilder<Class<? extends Command>, String> defaultRequiredCommands() {
        return FluentMapBuilder.<Class<? extends Command>, String>builder()
                .add(KillCommand.class, "Please make sure to mark who you've killed down so I can figure out who's won!")
                .add(ConcedeCommand.class, "If you'd like to concede, you can use this command!");
    }

    public boolean suppressOthersWinConditions() {
        return false;
    }

    public abstract boolean winConditions();

    public boolean loseConditions() {
        return false;
    }

    public boolean concedeConditions() {
        return false;
    }

    public void onNextRound(int i) {}

    // called when this role kills someone

    public void onKill(Member target) {

    }

    // called when ANYONE dies
    public void onDeath(Member killer, Member target) {
    }

    // called when the current role dies
    public void onSelfDeath(Member killer) {
    }

    public int minimumRequiredPlayers() {
        return 5;
    }

    protected EmbedBuilder startingEmbed() {
        return new EmbedBuilder()
                .setColor(color)
                .setAuthor(name())
                .setDescription("You are a " + name() +"!")
                .setFooter("please don't try to slide into my dms - you won't get anywhere ;)");
    }

    protected EmbedBuilder startingEmbed(String roleDescription) {
        return startingEmbed(roleDescription, null, null);
    }

    public EmbedBuilder startingEmbed(String roleDescription, String notesDescription) {
        return startingEmbed(roleDescription, notesDescription, null);
    }

    protected EmbedBuilder startingEmbed(String roleDescription, String notesDescription, String tipsDescription) {
        EmbedBuilder builder = startingEmbed();

        if (roleDescription != null && !roleDescription.isEmpty())
            builder.addField("Description", roleDescription, false);

        if (notesDescription != null && !notesDescription.isEmpty())
            builder.addField("Notes", notesDescription, false);

        if (tipsDescription != null && !tipsDescription.isEmpty())
            builder.addField("Tips & Tricks", tipsDescription, false);

        return builder;
    }
}
