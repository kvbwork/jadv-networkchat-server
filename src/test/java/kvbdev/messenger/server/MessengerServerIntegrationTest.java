package kvbdev.messenger.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static kvbdev.messenger.server.impl.ChatHandler.WHISPER_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class MessengerServerIntegrationTest {
    static final long TICK_VALUE = 50;
    static final long CONNECTION_TIMEOUT = TICK_VALUE * 2;
    static final String TEST_STRING = "TEST_STRING";
    static final String TEST_ADDRESS = "127.0.0.1";
    static final int TEST_PORT = 32555;
    static MessengerServer server;

    static class TestClient {
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

        public boolean tryEcho() {
            try {
                writeLine("/echo " + TEST_STRING);
                if (input.read() == -1) return false;
                readLine();
            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }

    public TestClient nextTestClient() throws IOException {
        Socket socket = new Socket(TEST_ADDRESS, TEST_PORT);
        return new TestClient("CLIENT@" + Integer.toHexString(socket.hashCode()), socket);
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MessengerServer(TEST_PORT);
        server.connectionsWorker.setConnectionTimeout(CONNECTION_TIMEOUT);
        server.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        server.close();
    }

    @Test
    void connection_success() throws IOException {
        TestClient testClient = nextTestClient();
        boolean isConnectionAlive = !testClient.socket.isClosed();

        assertThat(isConnectionAlive, is(true));
        testClient.socket.close();
    }

    @Test
    void disconnection_success() throws IOException {
        TestClient testClient = nextTestClient();
        testClient.socket.close();
        boolean isConnectionAlive = testClient.tryEcho();

        assertThat(isConnectionAlive, is(false));
    }

    @Test
    void timeout_success() throws InterruptedException, IOException {
        TestClient testClient = nextTestClient();
        boolean isAliveBeforeTimeout = testClient.tryEcho();
        Thread.sleep(CONNECTION_TIMEOUT + TICK_VALUE);
        boolean isAliveAfterTimeout = testClient.tryEcho();

        assertThat(isAliveBeforeTimeout, is(true));
        assertThat(isAliveAfterTimeout, is(false));
        testClient.socket.close();
    }

    @Test
    void login_and_sayAll_success() throws IOException {
        TestClient testClient1 = nextTestClient();
        TestClient testClient2 = nextTestClient();

        testClient1.writeLine("/login " + testClient1.name);
        testClient2.writeLine("/login " + testClient2.name);
        testClient2.writeLine(TEST_STRING);
        String receivedMessage = testClient1.readLine();

        assertThat(receivedMessage, containsString(TEST_STRING));

        testClient1.socket.close();
        testClient2.socket.close();
    }

    @Test
    void login_and_whisper_success() throws IOException {
        TestClient testClient1 = nextTestClient();
        TestClient testClient2 = nextTestClient();

        testClient1.writeLine("/login " + testClient1.name);
        testClient2.writeLine("/login " + testClient2.name);

        String text = WHISPER_PREFIX + testClient1.name + " " + TEST_STRING;
        testClient2.writeLine(text);
        String receivedMessage = testClient1.readLine();

        assertThat(receivedMessage, containsString(testClient2.name));
        assertThat(receivedMessage, containsString(testClient1.name));
        assertThat(receivedMessage, containsString(TEST_STRING));

        testClient1.socket.close();
        testClient2.socket.close();
    }

    @Test
    void login_and_exit_success() throws IOException, InterruptedException {
        TestClient testClient = nextTestClient();
        testClient.writeLine("/login " + testClient.name);
        boolean isAliveBeforeExit = testClient.tryEcho();

        testClient.writeLine("/exit");
        Thread.sleep(TICK_VALUE);
        boolean isAliveAfterExit = testClient.tryEcho();

        assertThat(isAliveBeforeExit, is(true));
        assertThat(isAliveAfterExit, is(false));

        testClient.socket.close();
    }

}