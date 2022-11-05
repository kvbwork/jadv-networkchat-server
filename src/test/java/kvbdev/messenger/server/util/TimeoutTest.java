package kvbdev.messenger.server.util;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static kvbdev.messenger.server.util.Timeout.INFINITE_TIMEOUT;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TimeoutTest {
    static final long TEST_TIMEOUT_VALUE = 50;

    Timeout timeout;

    @BeforeAll
    static void beforeAll() {
        Awaitility.setDefaultTimeout(TEST_TIMEOUT_VALUE * 2, MILLISECONDS);
        Awaitility.setDefaultPollInterval(10, MILLISECONDS);
    }

    @AfterAll
    static void afterAll() {
        Awaitility.reset();
    }

    @BeforeEach
    void setUp() {
        timeout = new Timeout();
    }

    @AfterEach
    void tearDown() {
        timeout = null;
    }

    @Test
    void isTimeout_true_success() {
        await().until(() -> timeout.isTimeout(TEST_TIMEOUT_VALUE), is(true));
    }

    @Test
    void isTimeout_false_success() {
        assertThat(timeout.isTimeout(TEST_TIMEOUT_VALUE), is(false));
    }

    @Test
    void isTimeout_infinite_false_success() {
        await().until(() -> timeout.isTimeout(TEST_TIMEOUT_VALUE), is(true));
        assertThat(timeout.isTimeout(INFINITE_TIMEOUT), is(false));
    }

    @Test
    void update_success() {
        await().until(() -> timeout.isTimeout(TEST_TIMEOUT_VALUE), is(true));
        timeout.update();
        assertThat(timeout.isTimeout(TEST_TIMEOUT_VALUE), is(false));
    }
}