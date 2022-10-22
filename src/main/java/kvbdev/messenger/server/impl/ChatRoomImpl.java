package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatRoomImpl implements ChatRoom {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomImpl.class);
    private final ConcurrentMap<String, UserContext> chatUsersMap = new ConcurrentHashMap<>();
    private final String chatRoomName;

    public ChatRoomImpl(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    @Override
    public String getRoomName() {
        return this.chatRoomName;
    }

    protected Optional<UserContext> findByName(String userName) {
        return Optional.ofNullable(chatUsersMap.get(userName));
    }

    @Override
    public void register(UserContext userContext) {
        String userName = userContext.getUser().getName();
        UserContext oldSession = chatUsersMap.get(userName);
        if (oldSession != null) unregister(oldSession);

        chatUsersMap.put(userName, userContext);
        logger.debug("register '{}' into '{}'", userName, getRoomName() );
    }

    @Override
    public void unregister(UserContext userContext) {
        String userName = userContext.getUser().getName();
        chatUsersMap.remove(userName);
        logger.debug("unregister '{}' from '{}'", userName, getRoomName());
    }

    @Override
    public Set<String> getUserNames() {
        return chatUsersMap.keySet();
    }

    @Override
    public void sendAll(String sourceUserName, String text) {
        if (text.isEmpty()) return;
        String message = sourceUserName + ": " + text;
        logger.trace("({}) {}: {}", getRoomName(), sourceUserName, text);
        chatUsersMap.values().stream()
                .filter(userContext -> !sourceUserName.equals(userContext.getUser().getName()))
                .forEach(userContext -> userContext.getConnection().writeLine(message));
    }

    @Override
    public void whisper(String sourceUserName, String targetUserName, String text) {
        if (text.isEmpty()) return;
        String message = sourceUserName + " -> " + targetUserName + ": " + text;
        logger.trace("({}) {} -> {}: {}", getRoomName(), sourceUserName, targetUserName, text);
        findByName(sourceUserName).ifPresent(
                fromUser -> findByName(targetUserName).ifPresentOrElse(
                        toUser -> toUser.getConnection().writeLine(message),
                        () -> fromUser.getConnection().writeLine("user " + targetUserName + " not found")
                )
        );
    }

    @Override
    public void dispose() {
        chatUsersMap.values()
                .forEach(userContext -> {
                    userContext.setChatRoom(null);
                    unregister(userContext);
                });
    }
}
