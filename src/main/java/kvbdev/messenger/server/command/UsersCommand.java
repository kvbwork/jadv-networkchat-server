package kvbdev.messenger.server.command;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;

import java.util.stream.Collectors;

public class UsersCommand extends AbstractCommand {
    protected final ChatRoom chatRoom;

    public UsersCommand(ChatRoom chatRoom) {
        super("/users");
        this.chatRoom = chatRoom;
    }

    @Override
    public void accept(Connection connection, String param) {
        if (connection.getContext().isEmpty()) return;

        String userNames = chatRoom.getUserNames().stream()
                .collect(Collectors.joining(", "));
        connection.writeLine(userNames);
    }
}
