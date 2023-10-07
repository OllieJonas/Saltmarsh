package me.olliejonas.saltmarsh;

import org.jooq.lambda.tuple.Tuple3;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) {
        Tuple3<String, String, String> argsTuple = args(args);
        String discToken = argsTuple.v1(); String spotifyId = argsTuple.v2(); String spotifySecret = argsTuple.v3();
        Saltmarsh saltmarsh = new Saltmarsh(discToken);

        try {
            saltmarsh.init();
        } catch (LoginException exception) {
            exception.printStackTrace();
        }
    }

    private static Tuple3<String, String, String> args(String[] args) {
        String discToken = args.length > 0 ? args[0] : System.getenv("SALTMARSH_DISCORD_TOKEN");
        String spotifyClientId = args.length > 1 ? args[1] : System.getenv("SALTMARSH_SPOTIFY_CLIENT_ID");
        String spotifyClientSecret = args.length > 2 ? args[2] : System.getenv("SALTMARSH_SPOTIFY_CLIENT_SECRET");

        if (discToken == null || discToken.equals(""))
            throw new IllegalArgumentException("discord token cannot be null!");

//        if (spotifyClientId == null || spotifyClientId.equals(""))
//            throw new IllegalArgumentException("spotify client id cannot be null!");
//
//        if (spotifyClientSecret == null || spotifyClientSecret.equals(""))
//            throw new IllegalArgumentException("spotify client secret cannot be null!");

        return new Tuple3<>(discToken, spotifyClientId, spotifyClientSecret);
    }
}
