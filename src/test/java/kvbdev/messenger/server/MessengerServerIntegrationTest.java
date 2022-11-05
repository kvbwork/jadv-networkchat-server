package kvbdev.messenger.server;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static kvbdev.messenger.server.impl.ChatHandler.WHISPER_PREFIX;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class MessengerServerIntegrationTest {
    static final long TEST_TIMEOUT_VALUE = 50;
    static final long LESS_THAN_TIMEOUT = TEST_TIMEOUT_VALUE - 25;
    static final long MORE_THAN_TIMEOUT = TEST_TIMEOUT_VALUE + 25;
    static final String TEST_STRING = "TEST_STRING";
    static final String TEST_ADDRESS = "127.0.0.1";
    static final int TEST_PORT = 32555;
    static MessengerServer server;

    static class TestClient implements Closeable {
        public final String name;
        public final Socket socket;
        public final BufferedReader input;
        public final Writer output;


        public TestClient(String name, Socket socket) throws IOException {
            this.name = name;
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new OutputStreamWriter(socket.getOutputStream());
        }

        public void writeLine(String text) throws IOException {
            output.write(text + "\n");
            output.flush();
        }

        public String readLine() throws IOException {
            return input.readLine();
        }

        public boolean tryKeepAlive() {
            try {
                writeLine("/echo " + TEST_STRING);
                if (input.read() == -1) return false;
                readLine();
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    public TestClient nextTestClient() throws IOException {
        Socket socket = new Socket(TEST_ADDRESS, TEST_PORT);
        return new TestClient("CLIENT@" + Integer.toHexString(socket.hashCode()), socket);
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        Awaitility.setDefaultTimeout(TEST_TIMEOUT_VALUE * 2, MILLISECONDS);
        Awaitility.setDefaultPollInterval(10, MILLISECONDS);

        server = new MessengerServer(TEST_PORT);
        server.connectionsWorker.setConnectionTimeout(TEST_TIMEOUT_VALUE);
        server.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        Awaitility.reset();
        server.close();
    }

    @Test
    void connection_success() throws IOException {
        try (TestClient testClient = nextTestClient()) {
            boolean isConnectionAlive = !testClient.socket.isClosed();

            assertThat(isConnectionAlive, is(true));
        }
    }

    @Test
    void disconnection_success() throws IOException {
        try (TestClient testClient = nextTestClient()) {
            testClient.socket.close();
            boolean isConnectionAlive = testClient.tryKeepAlive();

            assertThat(isConnectionAlive, is(false));
        }
    }

    @Test
    void timeout_success() throws IOException {
        try (TestClient testClient = nextTestClient()) {
            await().atMost(LESS_THAN_TIMEOUT, MILLISECONDS)
                    .until(testClient::tryKeepAlive, is(true));

            await().pollDelay(MORE_THAN_TIMEOUT, MILLISECONDS)
                    .until(testClient::tryKeepAlive, is(false));
        }
    }

    @Test
    void login_and_sayAll_success() throws IOException {
        try (TestClient testClient1 = nextTestClient();
             TestClient testClient2 = nextTestClient()) {

            testClient1.writeLine("/login " + testClient1.name);
            testClient2.writeLine("/login " + testClient2.name);
            testClient2.writeLine(TEST_STRING);
            String receivedMessage = testClient1.readLine();

            assertThat(receivedMessage, containsString(TEST_STRING));
        }
    }

    @Test
    void login_and_whisper_success() throws IOException {
        try (TestClient testClient1 = nextTestClient();
             TestClient testClient2 = nextTestClient()) {

            testClient1.writeLine("/login " + testClient1.name);
            testClient2.writeLine("/login " + testClient2.name);

            String text = WHISPER_PREFIX + testClient1.name + " " + TEST_STRING;
            testClient2.writeLine(text);
            String receivedMessage = testClient1.readLine();

            assertThat(receivedMessage, containsString(testClient2.name));
            assertThat(receivedMessage, containsString(testClient1.name));
            assertThat(receivedMessage, containsString(TEST_STRING));
        }
    }

    @Test
    void login_and_exit_success() throws IOException {
        try (TestClient testClient = nextTestClient()) {
            testClient.writeLine("/login " + testClient.name);

            await().atMost(TEST_TIMEOUT_VALUE, MILLISECONDS)
                    .until(testClient::tryKeepAlive, is(true));

            testClient.writeLine("/exit");

            await().atMost(TEST_TIMEOUT_VALUE, MILLISECONDS)
                    .until(testClient::tryKeepAlive, is(false));
        }
    }

}