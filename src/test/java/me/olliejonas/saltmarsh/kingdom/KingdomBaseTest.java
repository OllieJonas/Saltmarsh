package me.olliejonas.saltmarsh.kingdom;

import me.olliejonas.saltmarsh.kingdom.roles.Role;

import java.util.Collection;

public class KingdomBaseTest {

    protected KingdomGameRegistry registry;

    public KingdomBaseTest() {
        this.registry = new KingdomGameRegistry(null);
    }

    protected KingdomGame createTypicalGame(Collection<Role> expectedNonDefaultRoles) {
        return null;
    }
}
