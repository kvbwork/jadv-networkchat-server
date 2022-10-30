package kvbdev.messenger.server.runnable;

import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.ConnectionInputHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConnectionsWorkerTest {
    static final long TICK_VALUE = 25;
    static final String TEST_STRING = "TEST_STRING";

    ConnectionInputHandler testHandler;
    Connection testConnection;
    Collection<Connection> connectionsStorage;
    ConnectionsWorker sut;
    Thread connectionsWorkerThread;

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
    void thread_start_success() throws InterruptedException {
        connectionsWorkerThread.start();
        Thread.sleep(TICK_VALUE);
        assertThat(connectionsWorkerThread.isAlive(), is(true));
    }

    @Test
    void thread_interrupt_success() throws InterruptedException {
        connectionsWorkerThread.start();
        Thread.sleep(TICK_VALUE);
        connectionsWorkerThread.interrupt();
        Thread.sleep(TICK_VALUE);
        assertThat(connectionsWorkerThread.isAlive(), is(false));
    }

    @Test
    void connection_timeout_calls_close_success() throws InterruptedException {
        when(testConnection.isTimeout(anyLong())).thenReturn(true);
        connectionsWorkerThread.start();
        Thread.sleep(TICK_VALUE);
        verify(testConnection, atLeastOnce()).close();
    }

    @Test
    void remove_closed_connection_success() throws InterruptedException {
        when(testConnection.isClosed()).thenReturn(true);
        boolean containsBeforeClean = connectionsStorage.contains(testConnection);
        connectionsWorkerThread.start();
        Thread.sleep(TICK_VALUE);
        boolean containsAfterClean = connectionsStorage.contains(testConnection);
        assertThat(containsBeforeClean, is(true));
        assertThat(containsAfterClean, is(false));
    }

    @Test
    void read_connection_calls_handler_success() throws InterruptedException {
        when(testConnection.readLine()).thenReturn(Optional.of(TEST_STRING));
        connectionsWorkerThread.start();
        Thread.sleep(TICK_VALUE);
        verify(testHandler, atLeastOnce()).handle(eq(TEST_STRING), eq(testConnection));
    }
}