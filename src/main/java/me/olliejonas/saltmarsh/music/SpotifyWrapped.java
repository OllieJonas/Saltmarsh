package me.olliejonas.saltmarsh.music;

import lombok.Getter;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// haha get it because "Spotify Wrapped" but this is also a wrapper for the SpotifyApi object
public class SpotifyWrapped {

    static final Logger LOGGER = LoggerFactory.getLogger(SpotifyWrapped.class);

    private static final int DEFAULT_LOAD_AT_ONCE_LIMIT = -1;  // negative number = Spotify API max limit

    @Getter
    private SpotifyApi api;

    @Getter
    private final int loadAtOnceLimit;

    private final SpotifyTransformer transformer;

    private final ScheduledExecutorService executorService;

    @Getter
    private boolean isEnabled;

    public SpotifyWrapped(SpotifyApi api) {
        this(api, DEFAULT_LOAD_AT_ONCE_LIMIT);

    }
    public SpotifyWrapped(SpotifyApi api, int loadAtOnceLimit) {
        if (api == null)
            LOGGER.warn("No client id / secret was specified for Spotify! Feature has been disabled ...");

        this.api = api;
        this.loadAtOnceLimit = loadAtOnceLimit;

        this.transformer = new SpotifyTransformer(api);
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        this.isEnabled = api != null;

        refreshToken();
    }

    public void shutdown() {
        this.executorService.shutdownNow();
        this.api = null;
    }

    public List<String> transform(String link) throws IOException, ParseException, SpotifyWebApiException {
        return transform(link, loadAtOnceLimit);
    }
    public List<String> transform(String link, int limit) throws IOException, ParseException, SpotifyWebApiException {
        return transformer.transform(link, limit);
    }

    private void refreshToken() {
        if (!this.isEnabled) return;

        ClientCredentialsRequest request = api.clientCredentials().build();
        try {
            ClientCredentials credentials = request.execute();
            api.setAccessToken(credentials.getAccessToken());
            LOGGER.info("The Spotify Access Token has been refreshed! The new token will expire in " + credentials.getExpiresIn() + " seconds");
            this.executorService.schedule(this::refreshToken, credentials.getExpiresIn() - 1, TimeUnit.SECONDS);
            this.isEnabled = true;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.warn("Unable to refresh the Spotify access token! Disabling Spotify!");
            this.isEnabled = false;
        }
    }
}
