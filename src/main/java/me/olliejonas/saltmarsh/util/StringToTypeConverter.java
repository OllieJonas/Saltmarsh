package me.olliejonas.saltmarsh.util;

import net.dv8tion.jda.api.entities.*;
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
        OPTION_TO_CLASS.put(OptionType.NUMBER, Double.class);
        OPTION_TO_CLASS.put(OptionType.USER, Member.class);
        OPTION_TO_CLASS.put(OptionType.CHANNEL, GuildChannel.class);
        OPTION_TO_CLASS.put(OptionType.ROLE, Role.class);
        OPTION_TO_CLASS.put(OptionType.MENTIONABLE, IMentionable.class);
        OPTION_TO_CLASS.put(OptionType.ATTACHMENT, Message.Attachment.class);

        CLASS_TO_OPTION.put(String.class, OptionType.STRING);
        CLASS_TO_OPTION.put(Integer.class, OptionType.INTEGER);
        CLASS_TO_OPTION.put(Boolean.class, OptionType.BOOLEAN);
        CLASS_TO_OPTION.put(Double.class, OptionType.NUMBER);
        CLASS_TO_OPTION.put(Member.class, OptionType.USER);
        CLASS_TO_OPTION.put(GuildChannel.class, OptionType.CHANNEL);
        CLASS_TO_OPTION.put(Role.class, OptionType.ROLE);
        CLASS_TO_OPTION.put(IMentionable.class, OptionType.MENTIONABLE);
        CLASS_TO_OPTION.put(Message.Attachment.class, OptionType.ATTACHMENT);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> expandedCast(Guild guild, String string, Class<T> clazz) {
        Optional<T> cast = cast(string, clazz);
        if (cast.isPresent()) return cast;

        else if (clazz == Member.class) return Optional.ofNullable((T) toMember(guild, string));
        else if (clazz == GuildChannel.class) return Optional.ofNullable((T) toGuildChannel(guild, string));
        else if (clazz == Role.class) return Optional.ofNullable((T) toRole(guild, string));
        else return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> cast(String string, Class<T> clazz) {
        if (clazz == String.class) return Optional.ofNullable((T) string);
        else if (clazz == Boolean.class) return Optional.ofNullable((T) toBoolean(string));
        else if (clazz == Float.class) return Optional.ofNullable((T) toFloat(string));
        else if (clazz == Double.class) return Optional.ofNullable((T) toDouble(string));
        else if (clazz == Integer.class) return Optional.ofNullable((T) toInt(string));
        else return Optional.empty();
    }

    public static <T> boolean cantCast(String string, Class<T> clazz) {
        return cast(string, clazz).isEmpty();
    }

    public static <T> boolean cantExtendedCast(Guild guild, String string, Class<T> clazz) {
        return expandedCast(guild, string, clazz).isEmpty();
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

    private static GuildChannel toGuildChannel(Guild guild, String string) {
        try {
            return guild.getGuildChannelById(string.replaceAll("[<>#]", ""));
        } catch (Exception ignored) {
            return null;
        }
    }


    private static Role toRole(Guild guild, String string) {
        try {
            return guild.getRoleById(string.replaceAll("[<>@&]", ""));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Member toMember(Guild guild, String string) {
        try {
            String sanitised = string.replaceAll("[<>@]", "");
            Member member = guild.getMemberById(sanitised); // check cache before doing a sync rest action (slow!)
            if (member != null) return member;

            return guild.retrieveMemberById(sanitised).complete();
        } catch (Exception ignored) {
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
