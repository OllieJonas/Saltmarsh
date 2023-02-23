package me.olliejonas.saltmarsh;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) {
        String token = args.length > 0 ? args[0] : System.getenv("SALTMARSH_DISCORD_TOKEN");
        Saltmarsh saltmarsh = new Saltmarsh(token);

        try {
            saltmarsh.init();
        } catch (LoginException exception) {
            exception.printStackTrace();
        }
    }
}
