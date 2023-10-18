package me.olliejonas.saltmarsh.command.meta;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public record CommandPermissions(Collection<Permission> permissions) {
    public static final CommandPermissions ALL = new CommandPermissions(Collections.emptyList());

    public static final CommandPermissions ADMIN = new CommandPermissions(Collections.singleton(Permission.ADMINISTRATOR));

    public static final CommandPermissions EVENTS = new CommandPermissions(Set.of(Permission.MANAGE_EVENTS));


    public boolean hasPermission(Member member) {
        return member.hasPermission(permissions);
    }
}
