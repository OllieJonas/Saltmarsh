package me.olliejonas.saltmarsh.util.structures;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// not mine!
// here: https://github.com/vivekjustthink/WeakConcurrentHashMap
public class WeakConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    public static final long DEFAULT_EXPIRATION_TIME = 7;

    public static final TimeUnit DEFAULT_EXPIRATION_UNITS = TimeUnit.DAYS;

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<K, Long> timeMap = new ConcurrentHashMap<>();
    private WeakConcurrentHashMapListener<K, V> listener;

    private final long expiryInMillis;
    private boolean mapAlive = true;

    public WeakConcurrentHashMap() {
        this.expiryInMillis = DEFAULT_EXPIRATION_UNITS.toMillis(DEFAULT_EXPIRATION_TIME);
        initialize();
    }

    public WeakConcurrentHashMap(WeakConcurrentHashMapListener<K, V> listener) {
        this.listener = listener;
        this.expiryInMillis = 10000;
        initialize();
    }

    public WeakConcurrentHashMap(long expiryInMillis) {
        this.expiryInMillis = expiryInMillis;
        initialize();
    }

    public WeakConcurrentHashMap(long expiryInMillis, WeakConcurrentHashMapListener<K, V> listener) {
        this.expiryInMillis = expiryInMillis;
        this.listener = listener;
        initialize();
    }

    void initialize() {
        new CleanerThread().start();
    }

    public void registerRemovalListener(WeakConcurrentHashMapListener<K, V> listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if trying to insert values into map after quiting
     */
    @Override
    public V put(@NotNull K key, @NotNull V value) {
        if (!mapAlive) {
            throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");	// No I18N
        }
        Date date = new Date();
        timeMap.put(key, date.getTime());
        V returnVal = super.put(key, value);
        if (listener != null) {
            listener.notifyOnAdd(key, value);
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if trying to insert values into map after quiting
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (!mapAlive) {
            throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");	// No I18N
        }
        for (K key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if trying to insert values into map after quiting
     */
    @Override
    public V putIfAbsent(K key, V value) {
        if (!mapAlive) {
            throw new IllegalStateException("WeakConcurrent Hashmap is no more alive.. Try creating a new one.");	// No I18N
        }
        if (!containsKey(key)) {
            return put(key, value);
        } else {
            return get(key);
        }
    }

    /**
     * Should call this method when it's no longer required
     */
    public void quitMap() {
        mapAlive = false;
    }

    public boolean isAlive() {
        return mapAlive;
    }

    /**
     *
     * This thread performs the cleaning operation on the concurrent hashmap once in a specified interval. This wait interval is half of the
     * time from the expiry time.
     *
     *
     */
    class CleanerThread extends Thread {

        @Override
        public void run() {
            while (mapAlive) {
                cleanMap();
                try {
                    Thread.sleep(expiryInMillis / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void cleanMap() {
            long currentTime = new Date().getTime();
            for (K key : timeMap.keySet()) {
                if (currentTime > (timeMap.get(key) + expiryInMillis)) {
                    V value = remove(key);
                    timeMap.remove(key);
                    if (listener != null) {
                        listener.notifyOnRemoval(key, value);
                    }
                }
            }
        }
    }
}

