package kvbdev.messenger.server.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TimeoutTest {
    public static long TEST_TIMEOUT = 100;
    public static long TEST_TIMEOUT_STEP = 50;

    @Test
    void isTimeout_true_success() throws InterruptedException {
        Timeout timeout = new Timeout(TEST_TIMEOUT);
        long sleepValue = TEST_TIMEOUT + TEST_TIMEOUT_STEP;
        TimeUnit.MILLISECONDS.sleep(sleepValue);
        assertThat(timeout.isTimeout(), is(true));
    }

    @Test
    void isTimeout_false_success() throws InterruptedException {
        Timeout timeout = new Timeout(TEST_TIMEOUT);
        long sleepValue = TEST_TIMEOUT - TEST_TIMEOUT_STEP;
        TimeUnit.MILLISECONDS.sleep(sleepValue);
        assertThat(timeout.isTimeout(), is(false));
    }

    @Test
    void isTimeout_infinite_false_success() throws InterruptedException {
        Timeout timeout = new Timeout(0L);
        long sleepValue = TEST_TIMEOUT;
        TimeUnit.MILLISECONDS.sleep(sleepValue);
        assertThat(timeout.isTimeout(), is(false));
    }


    @Test
    void updateActivity() throws InterruptedException {
        Timeout timeout = new Timeout(TEST_TIMEOUT);
        long sleepValue = TEST_TIMEOUT + TEST_TIMEOUT_STEP;

        TimeUnit.MILLISECONDS.sleep(sleepValue);
        boolean timeoutFlag1 = timeout.isTimeout();

        timeout.updateActivity();

        TimeUnit.MILLISECONDS.sleep(TEST_TIMEOUT_STEP);
        boolean timeoutFlag2 = timeout.isTimeout();

        assertThat(timeoutFlag1, is(true));
        assertThat(timeoutFlag2, is(false));
    }
}