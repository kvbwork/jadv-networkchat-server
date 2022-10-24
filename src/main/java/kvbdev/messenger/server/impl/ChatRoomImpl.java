package kvbdev.messenger.server.impl;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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

    @Override
    public Optional<UserContext> findByName(String userName) {
        return Optional.ofNullable(chatUsersMap.get(userName));
    }

    @Override
    public void register(UserContext userContext) {
        String userName = userContext.getUser().getName();
        UserContext oldSession = chatUsersMap.get(userName);
        if (oldSession != null) unregister(oldSession);

        chatUsersMap.put(userName, userContext);
        logger.debug("register '{}' into '{}'", userName, getRoomName());
    }

    @Override
    public void unregister(UserContext userContext) {
        String userName = userContext.getUser().getName();
        chatUsersMap.remove(userName);
        logger.debug("unregister '{}' from '{}'", userName, getRoomName());
    }

    @Override
    public Collection<UserContext> getUsers() {
        return Collections.unmodifiableCollection(chatUsersMap.values());
    }

    @Override
    public boolean sendAll(String sourceUserName, String message) {
        if (message == null || message.isEmpty()) return false;
        String text = sourceUserName + ": " + message;

        logger.trace("({}) {}: {}", getRoomName(), sourceUserName, message);
        for (UserContext userContext : getUsers()) {
            if (sourceUserName.equals(userContext.getUser().getName())) continue;
            userContext.getConnection().writeLine(text);
        }
        return true;
    }

    @Override
    public boolean whisper(String sourceUserName, String targetUserName, String message) {
        if (message == null || message.isEmpty()) return false;
        if (targetUserName == null || targetUserName.isEmpty()) return false;
        String text = sourceUserName + " -> " + targetUserName + ": " + message;

        logger.trace("({}) {} -> {}: {}", getRoomName(), sourceUserName, targetUserName, message);
        findByName(sourceUserName).ifPresent(
                fromUser -> findByName(targetUserName).ifPresentOrElse(
                        toUser -> toUser.getConnection().writeLine(text),
                        () -> fromUser.getConnection().writeLine("user " + targetUserName + " not found")
                )
        );
        return true;
    }

    @Override
    public void dispose() {
        logger.debug("dispose {}", this);
        getUsers().forEach(userContext -> {
            userContext.setChatRoom(null);
            unregister(userContext);
        });
    }

}
