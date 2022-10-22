package kvbdev.messenger.server.runnable;

import kvbdev.messenger.server.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class ConnectionAcceptor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionAcceptor.class);
    private final ServerSocket serverSocket;
    private final Consumer<Connection> onConnection;

    public ConnectionAcceptor(int port, Consumer<Connection> onConnection) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.onConnection = onConnection;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (serverSocket.isClosed() || Thread.currentThread().isInterrupted()) break;
                Socket clientSocket = serverSocket.accept();
                try {
                    Connection connection = new Connection(clientSocket);
                    logger.debug("new {}", connection);
                    onConnection.accept(connection);
                } catch (IOException ex) {
                    logger.error("error connection creating for {}" + clientSocket);
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
