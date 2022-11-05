package kvbdev.messenger.server.runnable;

import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.ConnectionInputHandler;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConnectionsWorkerTest {
    static final String TEST_STRING = "TEST_STRING";

    ConnectionInputHandler testHandler;
    Connection testConnection;
    Collection<Connection> connectionsStorage;
    ConnectionsWorker sut;
    Thread connectionsWorkerThread;

    @BeforeAll
    static void beforeAll() {
        Awaitility.setDefaultPollInterval(25, MILLISECONDS);
        Awaitility.setDefaultTimeout(5, SECONDS);
    }

    @AfterAll
    static void afterAll() {
        Awaitility.reset();
    }

    @BeforeEach
    void setUp() {
        testHandler = mock(ConnectionInputHandler.class);
        when(testHandler.handle(anyString(), any(Connection.class))).thenReturn(true);

        testConnection = mock(Connection.class);
        when(testConnection.readLine()).thenReturn(Optional.empty());

        connectionsStorage = new LinkedList<>(List.of(testConnection));

        sut = new ConnectionsWorker(connectionsStorage, List.of(testHandler));

        connectionsWorkerThread = new Thread(sut);
    }

    @AfterEach
    void tearDown() {
        connectionsWorkerThread.interrupt();
        connectionsWorkerThread = null;
        connectionsStorage = null;
        testConnection = null;
        testHandler = null;
        sut = null;
    }

    @Test
    void thread_start_success() {
        connectionsWorkerThread.start();
        await().until(connectionsWorkerThread::isAlive);
    }

    @Test
    void thread_interrupt_success() {
        connectionsWorkerThread.start();
        await().until(connectionsWorkerThread::isAlive);
        connectionsWorkerThread.interrupt();
        await().until(() -> connectionsWorkerThread.isAlive(), is(false));
    }

    @Test
    void connection_timeout_calls_close_success() {
        when(testConnection.isTimeout(anyLong())).thenReturn(true);
        connectionsWorkerThread.start();
        await().untilAsserted(() -> verify(testConnection, atLeastOnce()).close());
    }

    @Test
    void remove_closed_connection_success() {
        when(testConnection.isClosed()).thenReturn(true);
        assertThat("contains before removing", connectionsStorage.contains(testConnection), is(true));
        connectionsWorkerThread.start();
        await("not contains after removing").until(() -> connectionsStorage.contains(testConnection), is(false));
    }

    @Test
    void read_connection_calls_handler_success() {
        when(testConnection.readLine()).thenReturn(Optional.of(TEST_STRING));
        connectionsWorkerThread.start();
        await("process the read string").untilAsserted(() ->
                verify(testHandler, atLeastOnce()).handle(eq(TEST_STRING), eq(testConnection)));
    }
}