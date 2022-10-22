package kvbdev.messenger.server;

import java.util.Optional;

public class UserContext implements Disposable {
    private final User user;
    private final Connection connection;
    private Optional<ChatRoom> chatRoom = Optional.empty();

    public UserContext(User user, Connection connection) {
        this.user = user;
        this.connection = connection;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "user=" + user.getName() +
                ", connection=" + connection.getSocket().getRemoteSocketAddress() +
                ", chatRoom=" + chatRoom.map(ChatRoom::getRoomName).orElse("empty") +
                '}';
    }

    public User getUser() {
        return user;
    }

    public Connection getConnection() {
        return connection;
    }

    public Optional<ChatRoom> getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = Optional.ofNullable(chatRoom);
    }

    @Override
    public void dispose() {
        chatRoom.ifPresent(chat -> {
            chat.unregister(this);
            setChatRoom(null);
        });
    }
}
