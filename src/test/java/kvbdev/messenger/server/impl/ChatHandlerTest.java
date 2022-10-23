package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.User;
import kvbdev.messenger.server.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class ChatHandlerTest {
    static final String TEST_STRING = "TEST_STRING";
    static final String TEST_USER_NAME = "TEST_USER";

    UserContext userContext;
    Connection connection;
    ChatRoom chatRoom;
    User user;
    ChatHandler sut;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        chatRoom = mock(ChatRoom.class);
        connection = mock(Connection.class);
        userContext = mock(UserContext.class);

        when(user.getName()).thenReturn(TEST_USER_NAME);

        when(connection.getContext()).thenReturn(Optional.of(userContext));

        when(userContext.getUser()).thenReturn(user);
        when(userContext.getChatRoom()).thenReturn(Optional.of(chatRoom));
        when(userContext.getConnection()).thenReturn(connection);

        sut = new ChatHandler();
    }

    @AfterEach
    void tearDown() {
        userContext = null;
        connection = null;
        chatRoom = null;
        user = null;
        sut = null;
    }

    @Test
    void handle_return_true_success() {
        boolean workPerformed = sut.handle(TEST_STRING, connection);
        assertThat(workPerformed, is(true));
    }

    @Test
    void handle_empty_UserContext_return_false() {
        when(connection.getContext()).thenReturn(Optional.empty());
        boolean workPerformed = sut.handle(TEST_STRING, connection);
        assertThat(workPerformed, is(false));
    }

    @Test
    void handle_empty_ChatRoom_return_false() {
        when(userContext.getChatRoom()).thenReturn(Optional.empty());
        boolean workPerformed = sut.handle(TEST_STRING, connection);
        assertThat(workPerformed, is(false));
    }

    @Test
    void handle_sayAll_success() {
        sut.handle(TEST_STRING, connection);
        verify(chatRoom, times(1)).sendAll(eq(TEST_USER_NAME), eq(TEST_STRING));
    }

    @Test
    void handle_whisper_success() {
        String targetUserName = "User2";
        String userInput = ChatHandler.WHISPER_PREFIX + targetUserName + " " + TEST_STRING;
        sut.handle(userInput, connection);
        verify(chatRoom, times(1)).whisper(eq(TEST_USER_NAME), eq(targetUserName), eq(TEST_STRING));
    }

    @Test
    void handle_whisper_no_message_failure() {
        String targetUserName = "User2";
        String userInput = ChatHandler.WHISPER_PREFIX + targetUserName;
        boolean workPerformed = sut.handle(userInput, connection);
        assertThat(workPerformed, is(false));
        verify(chatRoom, never()).whisper(anyString(), anyString(), anyString());
    }

    @Test
    void handle_whisper_no_target_username_failure() {
        String userInput = ChatHandler.WHISPER_PREFIX + " " + TEST_STRING;
        boolean workPerformed = sut.handle(userInput, connection);
        assertThat(workPerformed, is(false));
        verify(chatRoom, never()).whisper(anyString(), anyString(), anyString());
    }

}