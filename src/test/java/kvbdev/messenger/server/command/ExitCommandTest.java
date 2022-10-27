package kvbdev.messenger.server.command;

import kvbdev.messenger.server.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ExitCommandTest {
    Connection testConnection;
    ExitCommand sut;

    @BeforeEach
    void setUp() {
        testConnection = mock(Connection.class);
        sut = new ExitCommand();
    }

    @AfterEach
    void tearDown() {
        testConnection = null;
        sut = null;
    }

    @Test
    void close_opened_connection_success() {
        sut.accept(testConnection, null);
        verify(testConnection, times(1)).close();
    }

    @Test
    void close_closed_connection_failure() {
        when(testConnection.isClosed()).thenReturn(true);
        sut.accept(testConnection, null);
        verify(testConnection, never()).close();
    }

}