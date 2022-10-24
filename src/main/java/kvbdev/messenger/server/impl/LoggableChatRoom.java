package kvbdev.messenger.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

public class LoggableChatRoom extends ChatRoomImpl {
    private static final Logger logger = LoggerFactory.getLogger(LoggableChatRoom.class);

    protected Optional<String> filename;
    protected OutputStream out;
    protected DateTimeFormatter dateTimeFormatter;

    public LoggableChatRoom(String chatRoomName, String filename) throws IOException {
        this(chatRoomName, new FileOutputStream(filename, true));
        this.filename = Optional.of(filename);
    }

    public LoggableChatRoom(String chatRoomName, OutputStream outputStream) {
        super(chatRoomName);
        this.out = outputStream;
        this.dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        this.filename = Optional.empty();
    }

    @Override
    public boolean sendAll(String sourceUserName, String message) {
        String str = sourceUserName + ": " + message;
        logMessage(str);
        return super.sendAll(sourceUserName, message);
    }

    @Override
    public boolean whisper(String sourceUserName, String targetUserName, String message) {
        String str = sourceUserName + " -> " + targetUserName + ": " + message;
        logMessage(str);
        return super.whisper(sourceUserName, targetUserName, message);
    }

    protected void logMessage(String text) {
        String str = LocalDateTime.now().format(dateTimeFormatter) + " | " + text + "\n";
        try {
            out.write(str.getBytes());
            out.flush();
        } catch (IOException e) {
            logger.error("chat log writing error", e);
        }
    }

    @Override
    public void dispose() {
        if (filename.isPresent()) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                logger.error("error closing chat log", e);
            }
        }
        super.dispose();
    }
}
