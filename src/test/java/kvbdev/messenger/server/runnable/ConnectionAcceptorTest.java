package kvbdev.messenger.server.runnable;

import kvbdev.messenger.server.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ConnectionAcceptorTest {

    static class CallbackConnectionHolder implements Consumer<Connection> {
        Connection connection;

        @Override
        public void accept(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }

    }

    static long TICK_VALUE = 50;
    static int TEST_PORT = 30222;
    ConnectionAcceptor connectionAcceptor;
    Thread connectionAcceptorThread;
    CallbackConnectionHolder callbackConnectionHolder;


    @BeforeEach
    void setUp() throws IOException {
        callbackConnectionHolder = new CallbackConnectionHolder();
        connectionAcceptor = new ConnectionAcceptor(TEST_PORT, callbackConnectionHolder);
        connectionAcceptorThread = new Thread(connectionAcceptor);
        connectionAcceptorThread.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        connectionAcceptorThread.interrupt();
        connectionAcceptorThread = null;
        connectionAcceptor.close();
        connectionAcceptor = null;
        callbackConnectionHolder = null;
    }

    @Test
    void start_thread_success() throws InterruptedException {
        Thread.sleep(TICK_VALUE);
        assertThat(connectionAcceptorThread.isAlive(), is(true));
    }

    @Test
    void start_opens_ServerSocket_success() throws InterruptedException {
        Thread.sleep(TICK_VALUE);
        assertThat(connectionAcceptor.serverSocket.isClosed(), is(false));
    }

    @Test
    void closing_kills_thread_success() throws InterruptedException, IOException {
        Thread.sleep(TICK_VALUE);
        connectionAcceptor.close();
        Thread.sleep(TICK_VALUE);
        assertThat(connectionAcceptorThread.isAlive(), is(false));
    }

    @Test
    void closing_ServerSocket_success() throws InterruptedException, IOException {
        Thread.sleep(TICK_VALUE);
        connectionAcceptor.close();
        Thread.sleep(TICK_VALUE);
        assertThat(connectionAcceptor.serverSocket.isClosed(), is(true));
    }

    @Test
    void accept_Connection_success() throws IOException, InterruptedException {
        Socket localClientSocket = new Socket("127.0.0.1", TEST_PORT);
        Thread.sleep(TICK_VALUE);

        Connection connection = callbackConnectionHolder.getConnection();
        localClientSocket.close();

        assertThat(connection, is(notNullValue()));
    }
}