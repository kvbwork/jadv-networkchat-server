package kvbdev.messenger.server;

import java.util.Set;

public interface ChatRoom extends Disposable {

    String getRoomName();

    void register(UserContext userContext);

    void unregister(UserContext userContext);

    Set<String> getUserNames();

    void sendAll(String sourceUserName, String text);

    void whisper(String sourceUserName, String targetUserName, String text);
}
