package kvbdev.messenger.server.command;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.User;
import kvbdev.messenger.server.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

class UsersCommandTest {
    static final List<String> testUserNames = List.of("TEST_USER1", "TEST_USER2", "TEST_USER3");
    static final List<UserContext> testUsers = testUserNames.stream()
            .map(User::new)
            .map(user -> new UserContext(user, null))
            .collect(Collectors.toList());

    ChatRoom testChatRoom;
    Connection testConnection;
    UserContext testContext;
    UsersCommand sut;

    @BeforeEach
    void setUp() {
        testChatRoom = mock(ChatRoom.class);
        when(testChatRoom.getUsers()).thenReturn(testUsers);
        testContext = mock(UserContext.class);
        when(testContext.getChatRoom()).thenReturn(Optional.of(testChatRoom));
        testConnection = mock(Connection.class);
        when(testConnection.getContext()).thenReturn(Optional.of(testContext));
        sut = new UsersCommand();
    }

    @AfterEach
    void tearDown() {
        testChatRoom = null;
        testContext = null;
        testConnection = null;
        sut = null;
    }

    @Test
    void on_empty_context_failed() {
        reset(testConnection);
        sut.accept(testConnection, null);
        verify(testConnection, never()).writeLine(any());
    }

    @Test
    void call_connection_writeLine_success() {
        sut.accept(testConnection, null);
        verify(testConnection, times(1)).writeLine(anyString());
    }

    @Test
    void response_have_all_user_names_success() {
        sut.accept(testConnection, null);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(testConnection, times(1)).writeLine(stringCaptor.capture());

        String actualString = stringCaptor.getValue();
        testUserNames.forEach(userName -> assertThat(actualString, containsString(userName)));
    }

}