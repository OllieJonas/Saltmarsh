package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandInfo;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.util.StringToTypeConverter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;

public class WhatTypeIsCommand extends Command {

    public WhatTypeIsCommand() {
        super(CommandPermissions.ADMIN, "type", "what-type-is");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(new OptionData(OptionType.STRING, "thing", "thing you wanna get the type for", true));
    }

    @Override
    public CommandInfo info() {
        return CommandInfo.of("WHAT TIME IS IT RIGHT NOW.COM ????? (ADMIN)");
    }

    @Override
    public InteractionResponses execute(Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        Set<Class<?>> potentialClasses = StringToTypeConverter.CLASS_TO_OPTION.keySet();

        potentialClasses.remove(String.class);
        potentialClasses.remove(Boolean.class);

        String target = args.get("thing").getAsString().strip();

        Optional<?> match = Optional.empty();

        for (Class<?> clazz : potentialClasses) {
            try {
                match = StringToTypeConverter.expandedCast(channel.getGuild(), target, clazz);
                if (match.isPresent()) break;
            } catch (Throwable t) {
                System.out.println("caught an exception on " + clazz.getSimpleName());
                t.printStackTrace();
            }
        }


        return InteractionResponses.messageAsEmbed(match.isEmpty() ? "Failed to cast it to something useful!" : "This is a " + match.get().getClass().getSimpleName() + " !");
    }
}
