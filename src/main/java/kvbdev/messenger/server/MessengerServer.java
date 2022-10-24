package kvbdev.messenger.server;

import kvbdev.messenger.server.command.EchoCommand;
import kvbdev.messenger.server.command.ExitCommand;
import kvbdev.messenger.server.command.LoginCommand;
import kvbdev.messenger.server.command.UsersCommand;
import kvbdev.messenger.server.impl.ChatHandler;
import kvbdev.messenger.server.impl.CommandHandler;
import kvbdev.messenger.server.impl.LoggableChatRoom;
import kvbdev.messenger.server.runnable.ConnectionAcceptor;
import kvbdev.messenger.server.runnable.ConnectionsWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessengerServer implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(MessengerServer.class);
    private static final long DEFAULT_CONNECTION_TIMEOUT = 60_000;

    protected final int port;
    protected final ChatRoom chatRoom;
    protected final Collection<Connection> connectionsStorage;

    protected final ConnectionAcceptor connectionAcceptor;
    protected final ConnectionsWorker connectionsWorker;
    protected final ThreadGroup threadGroup;

    public MessengerServer(int port) throws IOException {
        this.port = port;
        this.chatRoom = new LoggableChatRoom("Main", "file.log");
        this.connectionsStorage = new ConcurrentLinkedQueue<>();
        this.threadGroup = new ThreadGroup("MessengerServer");

        CommandHandler commandHandler = new CommandHandler();
        commandHandler.register(new ExitCommand());
        commandHandler.register(new LoginCommand(chatRoom));
        commandHandler.register(new UsersCommand());
        commandHandler.register(new EchoCommand());

        ChatHandler chatHandler = new ChatHandler();

        connectionsWorker = new ConnectionsWorker(connectionsStorage, List.of(commandHandler, chatHandler));
        connectionsWorker.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);

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

    @Override
    public void close() throws IOException {
        if (threadGroup.activeCount() > 0) {
            threadGroup.interrupt();
            connectionAcceptor.close();
            cleanConnections();
            chatRoom.dispose();
            logger.info("MessengerServer was stopped");
        }
    }

    protected void cleanConnections() {
        connectionsStorage.forEach(Connection::close);
        connectionsStorage.clear();
    }

}
