package me.olliejonas.saltmarsh.util;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StringToTypeConverter {

    public static final Set<OptionType> STRING_REPRESENTED = Set.of(OptionType.USER,
            OptionType.CHANNEL,
            OptionType.ROLE,
            OptionType.MENTIONABLE);
    public static final Map<OptionType, Class<?>> OPTION_TO_CLASS = new HashMap<>();
    public static final Map<Class<?>, OptionType> CLASS_TO_OPTION = new HashMap<>();

    static {
        OPTION_TO_CLASS.put(OptionType.STRING, String.class);
        OPTION_TO_CLASS.put(OptionType.INTEGER, Integer.class);
        OPTION_TO_CLASS.put(OptionType.BOOLEAN, Boolean.class);
        OPTION_TO_CLASS.put(OptionType.USER, Member.class);
        OPTION_TO_CLASS.put(OptionType.CHANNEL, GuildChannel.class);
        OPTION_TO_CLASS.put(OptionType.ROLE, Role.class);
        OPTION_TO_CLASS.put(OptionType.MENTIONABLE, IMentionable.class);
        OPTION_TO_CLASS.put(OptionType.NUMBER, Double.class);
        OPTION_TO_CLASS.put(OptionType.ATTACHMENT, Message.Attachment.class);

        CLASS_TO_OPTION.put(String.class, OptionType.STRING);
        CLASS_TO_OPTION.put(Integer.class, OptionType.INTEGER);
        CLASS_TO_OPTION.put(Boolean.class, OptionType.BOOLEAN);
        CLASS_TO_OPTION.put(Member.class, OptionType.USER);
        CLASS_TO_OPTION.put(GuildChannel.class, OptionType.CHANNEL);
        CLASS_TO_OPTION.put(Role.class, OptionType.ROLE);
        CLASS_TO_OPTION.put(IMentionable.class, OptionType.MENTIONABLE);
        CLASS_TO_OPTION.put(Double.class, OptionType.NUMBER);
        CLASS_TO_OPTION.put(Message.Attachment.class, OptionType.ATTACHMENT);
    }

    public static <T> Optional<T> expandedCast(String string, Class<T> clazz) {
        Optional<T> simpleCast = cast(string, clazz);
        return simpleCast;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> cast(String string, Class<T> clazz) {
        if (clazz == String.class) return Optional.ofNullable((T) string);
        else if (clazz == Boolean.class) return Optional.ofNullable((T) toBoolean(string));
        else if (clazz == Float.class) return Optional.ofNullable((T) toFloat(string));
        else if (clazz == Double.class) return Optional.ofNullable((T) toDouble(string));
        else if (clazz == Integer.class) return Optional.ofNullable((T) toInt(string));
        else throw new IllegalArgumentException("casting to this class isn't supported! :(");
    }

    public static <T> boolean cantCast(String string, Class<T> clazz) {
        return cast(string, clazz).isEmpty();
    }

    private static Boolean toBoolean(String string) {
        if (string == null) return null;

        return Boolean.parseBoolean(string) ||
                string.equalsIgnoreCase("y") ||
                string.equalsIgnoreCase("yes") ||
                string.equalsIgnoreCase("✅") ||
                string.equalsIgnoreCase("\uD83D\uDC4D");  // thumbs up emoji
    }

    private static Float toFloat(String string) {
        if (string == null) return null;

        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Double toDouble(String string) {
        if (string == null) return null;

        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }


    private static Integer toInt(String string) {
        if (string == null) return null;

        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
