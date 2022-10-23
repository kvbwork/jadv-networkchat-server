package kvbdev.messenger.server.command;

import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.UserContext;

public class UsersCommand extends AbstractCommand {
    public UsersCommand() {
        super("/users");
    }

    @Override
    public void accept(Connection connection, String param) {
        if (connection.getContext().isEmpty()) return;
        UserContext context = connection.getContext().get();

        context.getChatRoom().ifPresent(
                chatRoom -> connection.writeLine(
                        String.join(", ", chatRoom.getUserNames())
                )
        );
    }
}
