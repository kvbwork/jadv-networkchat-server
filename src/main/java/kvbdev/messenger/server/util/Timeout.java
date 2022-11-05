package kvbdev.messenger.server.util;

public class Timeout {
    public static final int INFINITE_TIMEOUT = 0;
    private static final long NANOS_IN_MILLIS = 1_000_000;
    private volatile long lastActivity;

    public Timeout() {
        update();
    }

    public boolean isTimeout(long millis) {
        if (millis == INFINITE_TIMEOUT) return false;
        long timeoutValueNs = millis * NANOS_IN_MILLIS;
        return timeoutValueNs != 0 && (System.nanoTime() - lastActivity) > timeoutValueNs;
    }

    public void update() {
        lastActivity = System.nanoTime();
    }
}
