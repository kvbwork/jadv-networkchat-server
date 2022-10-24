package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.User;
import kvbdev.messenger.server.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class ChatRoomImplTest {
    static final String TEST_STRING = "TEST_STRING";
    static final String TEST_CHAT_ROOM_NAME = "TEST_CHAT_ROOM";
    static final String FIRST_USER_NAME = "FIRST_USER";
    static final String SECOND_USER_NAME = "SECOND_USER";

    User firstUser;
    UserContext firstUserContext;
    Connection firstUserConnection;

    User secondUser;
    UserContext secondUserContext;
    Connection secondUserConnection;

    ChatRoomImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ChatRoomImpl(TEST_CHAT_ROOM_NAME);

        firstUser = mock(User.class);
        firstUserContext = mock(UserContext.class);
        firstUserConnection = mock(Connection.class);

        when(firstUser.getName()).thenReturn(FIRST_USER_NAME);
        when(firstUserContext.getUser()).thenReturn(firstUser);
        when(firstUserContext.getConnection()).thenReturn(firstUserConnection);
        sut.register(firstUserContext);

        secondUser = mock(User.class);
        secondUserContext = mock(UserContext.class);
        secondUserConnection = mock(Connection.class);

        when(secondUser.getName()).thenReturn(SECOND_USER_NAME);
        when(secondUserContext.getUser()).thenReturn(secondUser);
        when(secondUserContext.getConnection()).thenReturn(secondUserConnection);
        sut.register(secondUserContext);
    }

    @AfterEach
    void tearDown() {
        sut = null;
        firstUserContext = null;
        firstUserConnection = null;
        firstUser = null;
        secondUserContext = null;
        secondUserConnection = null;
        secondUser = null;
    }

    @Test
    void getRoomName_success() {
        assertThat(sut.getRoomName(), is(TEST_CHAT_ROOM_NAME));
    }

    @Test
    void findByName_success() {
        UserContext userContext = sut.findByName(FIRST_USER_NAME).orElseThrow();
        assertThat(userContext, theInstance(firstUserContext));
    }

    @Test
    void register_success() {
        String testUserName = "TEST_USER";
        User testUser = mock(User.class);
        when(testUser.getName()).thenReturn(testUserName);

        UserContext testUserContext = mock(UserContext.class);
        when(testUserContext.getUser()).thenReturn(testUser);

        sut.register(testUserContext);
        assertThat(sut.findByName(testUserName).orElseThrow(), theInstance(testUserContext));
    }

    @Test
    void register_replace_success() {
        String testUserName = FIRST_USER_NAME;
        User testUser = mock(User.class);
        when(testUser.getName()).thenReturn(testUserName);

        UserContext testUserContext = mock(UserContext.class);
        when(testUserContext.getUser()).thenReturn(testUser);

        sut.register(testUserContext);
        assertThat(sut.findByName(testUserName).orElseThrow(), theInstance(testUserContext));
    }


    @Test
    void unregister_success() {
        UserContext testUserContext = firstUserContext;
        String testUserName = testUserContext.getUser().getName();

        assertThat(sut.findByName(testUserName).isPresent(), is(true));
        sut.unregister(testUserContext);
        assertThat(sut.findByName(testUserName).isEmpty(), is(true));
    }

    @Test
    void getUsers_success() {
        Iterable<UserContext> userNames = sut.getUsers();
        assertThat(userNames, hasItems(firstUserContext, secondUserContext));
    }

    @Test
    void dispose_success() {
        sut.dispose();
        assertThat(sut.getUsers(), not(hasItems(firstUserContext, secondUserContext)));
        assertThat(firstUserContext.getChatRoom().isEmpty(), is(true));
        assertThat(secondUserContext.getChatRoom().isEmpty(), is(true));
    }

    @Test
    void sendAll_success() {
        boolean workPerformed = sut.sendAll(FIRST_USER_NAME, TEST_STRING);
        assertThat(workPerformed, is(true));
        verify(firstUserConnection, never()).writeLine(anyString());
        verify(secondUserConnection, times(1)).writeLine(contains(FIRST_USER_NAME));
        verify(secondUserConnection, times(1)).writeLine(contains(TEST_STRING));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void sendAll_empty_message_failed(String message) {
        boolean workPerformed = sut.sendAll(FIRST_USER_NAME, message);
        assertThat(workPerformed, is(false));
        verify(secondUserConnection, never()).writeLine(anyString());
    }

    @Test
    void whisper_success() {
        boolean workPerformed = sut.whisper(FIRST_USER_NAME, SECOND_USER_NAME, TEST_STRING);
        assertThat(workPerformed, is(true));
        verify(firstUserConnection, never()).writeLine(anyString());
        verify(secondUserConnection, times(1)).writeLine(contains(FIRST_USER_NAME));
        verify(secondUserConnection, times(1)).writeLine(contains(SECOND_USER_NAME));
        verify(secondUserConnection, times(1)).writeLine(contains(TEST_STRING));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void whisper_empty_target_user_failed(String targetUserName) {
        boolean workPerformed = sut.whisper(FIRST_USER_NAME, targetUserName, TEST_STRING);
        assertThat(workPerformed, is(false));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void whisper_empty_message_failed(String message) {
        boolean workPerformed = sut.whisper(FIRST_USER_NAME, SECOND_USER_NAME, message);
        assertThat(workPerformed, is(false));
        verify(secondUserConnection, never()).writeLine(anyString());
    }

}