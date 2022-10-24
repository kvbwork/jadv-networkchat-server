package kvbdev.messenger.server;

import kvbdev.messenger.server.util.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Connection {
    private static final Charset DEFAULT_CHARSET = UTF_8;
    private static final int BUF_SIZE = 4096;
    private static final int SEPARATOR_CHAR = 10;     // '/n'

    private final ByteBuffer inputBuffer;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final Timeout timeout;

    private Optional<UserContext> context = Optional.empty();

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.inputBuffer = ByteBuffer.allocate(BUF_SIZE);
        this.timeout = new Timeout();
    }

    @Override
    public String toString() {
        return "Connection{" + socket.getRemoteSocketAddress() + "}";
    }

    public Socket getSocket() {
        return socket;
    }

    public Optional<UserContext> getContext() {
        return context;
    }

    public void setContext(UserContext context) {
        this.context = Optional.ofNullable(context);
    }

    public void close() {
        if (isClosed()) return;

        getContext().ifPresent(context -> {
            context.dispose();
            setContext(null);
        });

        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public boolean isClosed() {
        return socket.isClosed() && getContext().isEmpty();
    }

    public boolean isTimeout(long millis) {
        return timeout.isTimeout(millis);
    }

    public void keepAlive() {
        timeout.update();
    }

    public Optional<String> readLine() {
        try {
            while (in.available() > 0) {
                keepAlive();

                if (!inputBuffer.hasRemaining()) {
                    inputBuffer.flip();
                    return Optional.of(readBufToString(inputBuffer));
                }

                int byteVal = in.read();

                if (byteVal == -1) {
                    close();
                    break;
                }

                if (byteVal == SEPARATOR_CHAR) {
                    inputBuffer.flip();
                    return Optional.of(readBufToString(inputBuffer).trim());
                }

                inputBuffer.put((byte) byteVal);
            }
        } catch (IOException ex) {
            close();
        }
        return Optional.empty();
    }

    public void writeLine(String text) {
        try {
            out.write((text + "\n").getBytes(DEFAULT_CHARSET));
            out.flush();
        } catch (IOException e) {
            close();
        }
    }

    protected String readBufToString(ByteBuffer byteBuffer) {
        byte[] byteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArray);
        String result = new String(byteArray, DEFAULT_CHARSET);
        byteBuffer.clear();
        return result;
    }

}
