package me.olliejonas.saltmarsh.command.meta;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Collection;
import java.util.Collections;

public record CommandPermissions(Collection<Permission> permissions) {
    public static final CommandPermissions ALL = new CommandPermissions(Collections.emptyList());

    public static final CommandPermissions ADMIN = new CommandPermissions(Collections.singleton(Permission.ADMINISTRATOR));

    public boolean hasPermission(Member member) {
        return member.hasPermission(permissions);
    }
}
