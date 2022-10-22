package kvbdev.messenger.server;

import kvbdev.messenger.server.command.ExitCommand;
import kvbdev.messenger.server.command.LoginCommand;
import kvbdev.messenger.server.command.UsersCommand;
import kvbdev.messenger.server.impl.ChatHandler;
import kvbdev.messenger.server.impl.ChatRoomFileLog;
import kvbdev.messenger.server.impl.CommandHandler;
import kvbdev.messenger.server.runnable.ConnectionAcceptor;
import kvbdev.messenger.server.runnable.ConnectionsWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessengerServer {
    private static final Logger logger = LoggerFactory.getLogger(MessengerServer.class);

    private final int port;
    private final ChatRoom chatRoom;
    private final Collection<Connection> connectionsStorage;

    private final ConnectionAcceptor connectionAcceptor;
    private final ConnectionsWorker connectionsWorker;
    private final ThreadGroup threadGroup;

    public MessengerServer(int port) throws IOException {
        this.port = port;
        this.chatRoom = new ChatRoomFileLog("Common", "file.log");
        this.connectionsStorage = new ConcurrentLinkedQueue<>();
        this.threadGroup = new ThreadGroup("MessengerServer");

        CommandHandler commandHandler = new CommandHandler();
        commandHandler.register(new ExitCommand());
        commandHandler.register(new LoginCommand(chatRoom));
        commandHandler.register(new UsersCommand(chatRoom));

        ChatHandler chatHandler = new ChatHandler();

        connectionsWorker = new ConnectionsWorker(connectionsStorage, List.of(commandHandler, chatHandler));
        connectionAcceptor = new ConnectionAcceptor(port, connectionsStorage::add);
    }

    public int getPort() {
        return port;
    }

    public void start() {
        if (threadGroup.activeCount() > 0) return;
        new Thread(threadGroup, connectionAcceptor, "ConnectionAcceptor").start();
        new Thread(threadGroup, connectionsWorker, "ConnectionsHandler").start();
        logger.info("MessengerServer running on port: {}", getPort());
    }

    public void stop() {
        if (threadGroup.activeCount() > 0) {
            connectionsWorker.clear();
            threadGroup.interrupt();
            chatRoom.dispose();
            logger.info("MessengerServer was stopped");
        }
    }
}
