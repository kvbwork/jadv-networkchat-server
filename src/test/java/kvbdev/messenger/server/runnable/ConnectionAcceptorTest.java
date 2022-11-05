package kvbdev.messenger.server.runnable;

import kvbdev.messenger.server.Connection;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class ConnectionAcceptorTest {

    interface TestConnectionConsumer extends Consumer<Connection> {
    }

    static int TEST_PORT = 30222;
    ConnectionAcceptor connectionAcceptor;
    Thread connectionAcceptorThread;
    TestConnectionConsumer testConnectionConsumer;

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
    void setUp() throws IOException {
        testConnectionConsumer = mock(TestConnectionConsumer.class);
        connectionAcceptor = new ConnectionAcceptor(TEST_PORT, testConnectionConsumer);
        connectionAcceptorThread = new Thread(connectionAcceptor);
        connectionAcceptorThread.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        connectionAcceptorThread.interrupt();
        connectionAcceptorThread = null;
        connectionAcceptor.close();
        connectionAcceptor = null;
        testConnectionConsumer = null;
    }

    @Test
    void start_thread_success() {
        await().until(connectionAcceptorThread::isAlive);
    }

    @Test
    void start_opens_ServerSocket_success() {
        await().until(() -> connectionAcceptor.serverSocket.isClosed(), is(false));
    }

    @Test
    void closing_kills_thread_success() throws IOException {
        connectionAcceptor.close();
        await().until(() -> connectionAcceptorThread.isAlive(), is(false));
    }

    @Test
    void closing_ServerSocket_success() throws IOException {
        connectionAcceptor.close();
        await().until(() -> connectionAcceptor.serverSocket.isClosed(), is(true));
    }

    @Test
    void accept_Connection_success() throws IOException {
        Socket localClientSocket = new Socket("127.0.0.1", TEST_PORT);
        ArgumentCaptor<Connection> connectionArgumentCaptor = ArgumentCaptor.forClass(Connection.class);

        await().untilAsserted(() ->
                verify(testConnectionConsumer, times(1)).accept(connectionArgumentCaptor.capture()));

        int capturedClientLocalPort = connectionArgumentCaptor.getValue().getSocket().getPort();
        assertThat(capturedClientLocalPort, equalTo(localClientSocket.getLocalPort()));

        localClientSocket.close();
    }
}