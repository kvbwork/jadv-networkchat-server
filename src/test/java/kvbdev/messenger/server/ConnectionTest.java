package kvbdev.messenger.server;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class ConnectionTest {
    static final long TEST_TIMEOUT = 100;
    static final long TEST_TIMEOUT_STEP = 50;
    static final String TEST_STRING = "TEST_STRING";

    UserContext testContext;
    InputStream in;
    OutputStream out;
    Socket socket;
    Connection sut;

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
        Mockito.verify(socket, times(1)).close();
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
    void isTimeout_false_success() throws InterruptedException {
        long sleepValue = TEST_TIMEOUT - TEST_TIMEOUT_STEP;
        Thread.sleep(sleepValue);
        assertThat(sut.isTimeout(TEST_TIMEOUT), is(false));
    }

    @Test
    void isTimeout_true_success() throws InterruptedException {
        long sleepValue = TEST_TIMEOUT + TEST_TIMEOUT_STEP;
        Thread.sleep(sleepValue);
        assertThat(sut.isTimeout(TEST_TIMEOUT), is(true));
    }

    @Test
    void keepAlive_success() throws InterruptedException {
        long sleepValue = TEST_TIMEOUT + TEST_TIMEOUT_STEP;
        Thread.sleep(sleepValue);

        boolean timeoutTrue = sut.isTimeout(TEST_TIMEOUT);
        sut.keepAlive();

        assertThat(timeoutTrue, is(true));
        assertThat(sut.isTimeout(TEST_TIMEOUT), is(false));
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
    void writeLine_calls_OutputStream_write_success() throws IOException {
        sut.writeLine(TEST_STRING);
        Mockito.verify(out, atLeastOnce()).write(any(byte[].class));
    }

    @Test
    void readBufToString() {
        ByteBuffer buf = ByteBuffer.wrap(TEST_STRING.getBytes());
        String result = sut.readBufToString(buf);
        assertThat(result, equalTo(TEST_STRING));
    }
}