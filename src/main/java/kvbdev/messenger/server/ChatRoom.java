package kvbdev.messenger.server;

import java.util.Collection;
import java.util.Optional;

public interface ChatRoom extends Disposable {

    String getRoomName();

    Optional<UserContext> findByName(String userName);

    Collection<UserContext> getUsers();

    void register(UserContext userContext);

    void unregister(UserContext userContext);

    boolean sendAll(String sourceUserName, String message);

    boolean whisper(String sourceUserName, String targetUserName, String message);

}
