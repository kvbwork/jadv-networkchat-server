package kvbdev.messenger.server.command;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockSettings;
import org.mockito.internal.creation.MockSettingsImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

class LoginCommandTest {
    ChatRoom testChatRoom;
    Connection testConnection;
    LoginCommand sut;

    @BeforeEach
    void setUp() {
        MockSettings defaultReturnMockSettings = new MockSettingsImpl<>().defaultAnswer(RETURNS_MOCKS);

        testChatRoom = mock(ChatRoom.class);
        testConnection = mock(Connection.class, defaultReturnMockSettings);

        sut = new LoginCommand(testChatRoom);
    }

    @AfterEach
    void tearDown() {
        testChatRoom = null;
        testConnection = null;
        sut = null;
    }

    @Test
    void accept_solid_user_name_set_new_context_success() {
        String testUserName = "User1";
        sut.accept(testConnection, testUserName);

        ArgumentCaptor<UserContext> userContextCaptor = ArgumentCaptor.forClass(UserContext.class);
        verify(testConnection, times(1)).setContext(userContextCaptor.capture());

        String actualUserName = userContextCaptor.getValue().getUser().getName();
        assertThat(actualUserName, equalTo(testUserName));
    }

    @Test
    void accept_splitted_user_name_set_new_context_success() {
        String testUserName = "User Name";
        String expectedUserName = "User";
        sut.accept(testConnection, testUserName);

        ArgumentCaptor<UserContext> userContextCaptor = ArgumentCaptor.forClass(UserContext.class);
        verify(testConnection, times(1)).setContext(userContextCaptor.capture());

        String actualUserName = userContextCaptor.getValue().getUser().getName();
        assertThat(actualUserName, equalTo(expectedUserName));
    }

    @Test
    void accept_user_name_set_context_chat_room_success() {
        String testUserName = "User1";
        sut.accept(testConnection, testUserName);

        ArgumentCaptor<UserContext> userContextCaptor = ArgumentCaptor.forClass(UserContext.class);
        verify(testConnection, times(1)).setContext(userContextCaptor.capture());

        ChatRoom actualChatRoom = userContextCaptor.getValue().getChatRoom().orElseThrow();
        assertThat(actualChatRoom, equalTo(testChatRoom));
    }


    @ParameterizedTest
    @NullAndEmptySource
    void accept_empty_param_failed(String userName) {
        sut.accept(testConnection, userName);
        verify(testConnection, never()).setContext(any());
    }

}