package kvbdev.messenger.server.command;

import kvbdev.messenger.server.ChatRoom;
import kvbdev.messenger.server.Connection;
import kvbdev.messenger.server.User;
import kvbdev.messenger.server.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(LoginCommand.class);
    protected final ChatRoom chatRoom;

    public LoginCommand(ChatRoom chatRoom) {
        super("/login");
        this.chatRoom = chatRoom;
    }

    @Override
    public void accept(Connection connection, String param) {
        if (connection.getContext().isPresent()) return;

        if (param != null && !param.isEmpty()) {
            User user = new User(param);

            UserContext context = new UserContext(user, connection);
            connection.setContext(context);

            logger.debug("new {}", context);

            chatRoom.register(context);
            context.setChatRoom(chatRoom);
        }
    }

}
