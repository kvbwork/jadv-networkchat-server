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
        Timeout timeout = new Timeout();
        long sleepValue = TEST_TIMEOUT + TEST_TIMEOUT_STEP;
        TimeUnit.MILLISECONDS.sleep(sleepValue);
        assertThat(timeout.isTimeout(TEST_TIMEOUT), is(true));
    }

    @Test
    void isTimeout_false_success() throws InterruptedException {
        Timeout timeout = new Timeout();
        long sleepValue = TEST_TIMEOUT - TEST_TIMEOUT_STEP;
        TimeUnit.MILLISECONDS.sleep(sleepValue);
        assertThat(timeout.isTimeout(TEST_TIMEOUT), is(false));
    }

    @Test
    void isTimeout_infinite_false_success() throws InterruptedException {
        Timeout timeout = new Timeout();
        long sleepValue = TEST_TIMEOUT;
        TimeUnit.MILLISECONDS.sleep(sleepValue);
        assertThat(timeout.isTimeout(0), is(false));
    }

    @Test
    void update_success() throws InterruptedException {
        Timeout timeout = new Timeout();
        long sleepValue = TEST_TIMEOUT + TEST_TIMEOUT_STEP;

        TimeUnit.MILLISECONDS.sleep(sleepValue);
        boolean timeoutBeforeUpdate = timeout.isTimeout(TEST_TIMEOUT);

        timeout.update();

        TimeUnit.MILLISECONDS.sleep(TEST_TIMEOUT_STEP);
        boolean timeoutAfterUpdate = timeout.isTimeout(TEST_TIMEOUT);

        assertThat(timeoutBeforeUpdate, is(true));
        assertThat(timeoutAfterUpdate, is(false));
    }
}