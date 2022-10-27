package kvbdev.messenger.server.command;

import kvbdev.messenger.server.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.Mockito.*;

class EchoCommandTest {
    Connection testConnection;
    EchoCommand sut;

    @BeforeEach
    void setUp() {
        testConnection = mock(Connection.class);
        sut = new EchoCommand();
    }

    @AfterEach
    void tearDown() {
        testConnection = null;
        sut = null;
    }

    @ParameterizedTest
    @NullAndEmptySource
    void accept_empty_send_failed(String param) {
        sut.accept(testConnection, param);
        verify(testConnection, never()).writeLine(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"solid", "splitted param", "юникод"})
    void accept_message_send_success(String param) {
        sut.accept(testConnection, param);
        verify(testConnection, times(1)).writeLine(eq(param));
    }

}