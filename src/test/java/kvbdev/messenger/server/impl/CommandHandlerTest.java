package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.command.Command;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import static org.mockito.Mockito.*;

class CommandHandlerTest {
    static final String TEST_STRING = "TEST_STRING";
    static final String TEST_COMMAND_NAME = "/test_command";

    Command testCommand;
    Connection connection;
    CommandHandler sut;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        sut = new CommandHandler();

        testCommand = mock(Command.class);
        when(testCommand.getName()).thenReturn(TEST_COMMAND_NAME);
        sut.register(testCommand);
    }

    @AfterEach
    void tearDown() {
        testCommand = null;
        connection = null;
        sut = null;
    }

    @Test
    void register_success() {
        String commandName = "/another_test_command";
        Command testCmd = mock(Command.class);
        when(testCmd.getName()).thenReturn(commandName);
        sut.register(testCmd);
        assertThat(sut.commandMap.get(commandName), theInstance(testCmd));
    }

    @Test
    void handle_command_not_found_success() {
        String requestCommandName = "/another_test_command";
        boolean workPerformed = sut.handle(requestCommandName, connection);
        assertThat(workPerformed, is(false));
    }

    @Test
    void handle_command_no_param_success() {
        sut.handle(TEST_COMMAND_NAME, connection);
        Mockito.verify(testCommand, times(1)).accept(eq(connection), eq(""));
    }

    @Test
    void handle_command_with_param_success() {
        String paramText = TEST_STRING;
        String userInput = TEST_COMMAND_NAME + " " + paramText;
        sut.handle(userInput, connection);
        Mockito.verify(testCommand, times(1)).accept(eq(connection), eq(paramText));
    }

}