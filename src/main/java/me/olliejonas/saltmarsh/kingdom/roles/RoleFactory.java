package me.olliejonas.saltmarsh.kingdom.roles;

import me.olliejonas.saltmarsh.kingdom.KingdomGame;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleFactory {

    public static final Set<Class<? extends Role>> ALL_CLASSES = Set.of(
            Bandit.class,
            Challenger.class,
            Jester.class,
            King.class,
            Knight.class,
            Usurper.class,
            Wizard.class
    );

    public static final Map<String, Class<? extends Role>> NAME_TO_CLASS_MAP = ALL_CLASSES.stream()
            .collect(Collectors.toMap(e -> e.getSimpleName().toLowerCase(), e -> e));

    public static Role factory(String name, KingdomGame game) throws IllegalArgumentException {
        return factory(factory(name), game);
    }

    public static Class<? extends Role> factory(String name) throws IllegalArgumentException {
        return Optional.ofNullable(NAME_TO_CLASS_MAP.get(name.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException(name + " isn't a valid argument for roles!"));
    }

    public static Role factory(Class<? extends Role> role, KingdomGame game) {
        try {
            Constructor<? extends Role> roleConstructor = role.getConstructor(KingdomGame.class);
            return roleConstructor.newInstance(game);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean exists(String name) {
        return NAME_TO_CLASS_MAP.containsKey(name);
    }

}
