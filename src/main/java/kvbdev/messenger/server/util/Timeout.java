package kvbdev.messenger.server.util;

public class Timeout {
    private static final long NANOS_IN_MILLIS = 1_000_000;
    private final long timeoutValueNs;
    private volatile long lastActivity;

    public Timeout(long millis) {
        this.timeoutValueNs = millis * NANOS_IN_MILLIS;
        updateActivity();
    }

    public boolean isTimeout() {
        return timeoutValueNs != 0 && (System.nanoTime() - lastActivity) > timeoutValueNs;
    }

    public void updateActivity() {
        lastActivity = System.nanoTime();
    }
}
