package kvbdev.messenger.server.runnable;

import kvbdev.messenger.server.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class ConnectionAcceptor implements Runnable, Closeable {
    private static final long DEFAULT_CONNECTION_TIMEOUT = 60_000;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionAcceptor.class);

    protected ServerSocket serverSocket;
    protected final Consumer<Connection> onConnection;

    public ConnectionAcceptor(int port, Consumer<Connection> onConnection) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.onConnection = onConnection;
    }

    @Override
    public void run() {
        try {
            while (!(serverSocket.isClosed() || Thread.currentThread().isInterrupted())) {
                Socket clientSocket = serverSocket.accept();
                try {
                    Connection connection = new Connection(clientSocket, DEFAULT_CONNECTION_TIMEOUT);
                    logger.debug("new {}", connection);
                    onConnection.accept(connection);
                } catch (IOException ex) {
                    logger.error("connection IO error: {} ({})", ex.getMessage(), clientSocket);
                }
            }
            serverSocket.close();
        } catch (SocketException e) {
            logger.warn("{}", e.getMessage());
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
