package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.ConnectionInputHandler;
import kvbdev.messenger.server.UserContext;

public class ChatHandler implements ConnectionInputHandler {
    public static final String WHISPER_PREFIX = "@";

    @Override
    public boolean handle(String text, Connection connection) {
        if (connection.getContext().isEmpty()) return false;

        UserContext context = connection.getContext().get();
        String userName = context.getUser().getName();

        if (context.getChatRoom().isEmpty()) return false;

        ChatRoom chatRoom = context.getChatRoom().get();

        if (text.startsWith(WHISPER_PREFIX)) {
            final int partsCount = 2;
            String[] parts = text.split(" ", partsCount);
            if (parts.length < partsCount) return false;
            if (WHISPER_PREFIX.equals(parts[0].trim())) return false;

            final int nameStartIndex = 1;
            String targetUserName = parts[0].substring(nameStartIndex);
            String message = parts[1];

            chatRoom.whisper(userName, targetUserName, message);
        } else {
            chatRoom.sendAll(userName, text);
        }
        return true;
    }

}
