package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.ConnectionInputHandler;
import kvbdev.messenger.server.UserContext;

public class ChatHandler implements ConnectionInputHandler {
    private static final String COMMAND_PREFIX = "@";

    @Override
    public boolean handle(String text, Connection connection) {
        if (connection.getContext().isEmpty()) return false;

        UserContext context = connection.getContext().get();
        String userName = context.getUser().getName();

        if (context.getChatRoom().isEmpty()) return false;

        ChatRoom chatRoom = context.getChatRoom().get();

        if (text.startsWith(COMMAND_PREFIX)) {
            int spaceIndex = text.indexOf(" ");
            if (spaceIndex == -1 || spaceIndex == (text.length() - 1)) return false;
            String targetUserName = text.substring(1, spaceIndex);
            String message = text.substring(spaceIndex + 1);
            chatRoom.whisper(userName, targetUserName, message);
        } else {
            chatRoom.sendAll(userName, text);
        }
        return true;
    }

}
