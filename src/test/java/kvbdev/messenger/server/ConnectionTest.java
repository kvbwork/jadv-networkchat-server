package kvbdev.messenger.server;

import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class ConnectionTest {
    static final long TEST_TIMEOUT_VALUE = 50;
    static final long LESS_THAN_TIMEOUT = TEST_TIMEOUT_VALUE - 25;
    static final long MORE_THAN_TIMEOUT = TEST_TIMEOUT_VALUE + 25;
    static final String TEST_STRING = "TEST_STRING";

    UserContext testContext;
    InputStream in;
    OutputStream out;
    Socket socket;
    Connection sut;

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
    void setUp() throws IOException {
        testContext = mock(UserContext.class);
        in = mock(InputStream.class);
        out = mock(OutputStream.class);
        socket = mock(Socket.class);

        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);

        sut = new Connection(socket);
    }

    @AfterEach
    void tearDown() {
        sut = null;
        socket = null;
        in = null;
        out = null;
    }

    @Test
    void context_new_empty() {
        assertThat(sut.getContext().isEmpty(), is(true));
    }

    @Test
    void context_set_get_success() {
        sut.setContext(testContext);
        assertThat(sut.getContext().orElseThrow(), Matchers.sameInstance(testContext));
    }

    @Test
    void close_socket_success() throws IOException {
        sut.close();
        verify(socket, times(1)).close();
    }

    @Test
    void close_removes_context_success() {
        sut.setContext(testContext);
        sut.close();
        assertThat(sut.getContext().isEmpty(), is(true));
    }

    @Test
    void isClosed_false() {
        sut.setContext(testContext);
        assertThat(sut.isClosed(), is(false));
    }

    @Test
    void isClosed_true() {
        sut.setContext(null);
        when(socket.isClosed()).thenReturn(true);
        assertThat(sut.isClosed(), is(true));
    }

    @Test
    void isTimeout_false_success() {
        await().atMost(LESS_THAN_TIMEOUT, MILLISECONDS)
                .until(() -> sut.isTimeout(TEST_TIMEOUT_VALUE), is(false));
    }

    @Test
    void isTimeout_true_success() {
        await().atMost(MORE_THAN_TIMEOUT, MILLISECONDS)
                .until(() -> sut.isTimeout(TEST_TIMEOUT_VALUE), is(true));
    }

    @Test
    void keepAlive_update_timeout_success() {
        await().atMost(MORE_THAN_TIMEOUT, MILLISECONDS)
                .until(() -> sut.isTimeout(TEST_TIMEOUT_VALUE), is(true));

        sut.keepAlive();

        assertThat(sut.isTimeout(TEST_TIMEOUT_VALUE), is(false));
    }

    @Test
    void readLine_read_empty_success() {
        assertThat(sut.readLine().isEmpty(), is(true));
    }

    @Test
    void readLine_read_line_success() throws IOException {
        String expectedString = TEST_STRING;

        byte[] stringBytes = (expectedString + "\n").getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringBytes);
        when(in.available()).then(mock -> byteArrayInputStream.available());
        when(in.read()).then(mock -> byteArrayInputStream.read());

        String result = sut.readLine().orElseThrow();

        assertThat(result, equalTo(expectedString));
    }

    @Test
    void readLine_update_timeout_success() throws IOException {
        String expectedString = TEST_STRING;

        byte[] stringBytes = (expectedString + "\n").getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringBytes);
        when(in.available()).then(mock -> byteArrayInputStream.available());
        when(in.read()).then(mock -> byteArrayInputStream.read());

        await().atMost(MORE_THAN_TIMEOUT, MILLISECONDS)
                .until(() -> sut.isTimeout(TEST_TIMEOUT_VALUE), is(true));

        String readResult = sut.readLine().orElseThrow();
        boolean timeoutAfterDataRead = sut.isTimeout(TEST_TIMEOUT_VALUE);

        assertThat(readResult, equalTo(expectedString));
        assertThat(timeoutAfterDataRead, is(false));
    }

    @Test
    void readLine_update_timeout_failure(){
        await().atMost(MORE_THAN_TIMEOUT, MILLISECONDS)
                .until(() -> sut.isTimeout(TEST_TIMEOUT_VALUE), is(true));

        Optional<String> readResult = sut.readLine();
        boolean timeoutAfterDataRead = sut.isTimeout(TEST_TIMEOUT_VALUE);

        assertThat(readResult.isEmpty(), is(true));
        assertThat(timeoutAfterDataRead, is(true));
    }

    @Test
    void writeLine_calls_OutputStream_write_success() throws IOException {
        sut.writeLine(TEST_STRING);
        verify(out, atLeastOnce()).write(any(byte[].class));
    }

    @Test
    void readBufToString_success() {
        ByteBuffer buf = ByteBuffer.wrap(TEST_STRING.getBytes());
        String result = sut.readBufToString(buf);
        assertThat(result, equalTo(TEST_STRING));
    }
}