package kvbdev.messenger.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ChatRoomFileLog extends ChatRoomImpl {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomFileLog.class);

    protected final String filename;
    protected FileOutputStream out;
    protected DateTimeFormatter dateTimeFormatter;

    public ChatRoomFileLog(String chatRoomName, String filename) throws IOException {
        super(chatRoomName);
        this.filename = filename;
        this.out = new FileOutputStream(filename, true);
        this.dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    }

    @Override
    public void sendAll(String sourceUserName, String text) {
        super.sendAll(sourceUserName, text);
        String str = sourceUserName + ": " + text;
        logMessage(str);
    }

    @Override
    public void whisper(String sourceUserName, String targetUserName, String text) {
        super.whisper(sourceUserName, targetUserName, text);
        String str = sourceUserName + " -> " + targetUserName + ": " + text;
        logMessage(str);
    }

    protected void logMessage(String text) {
        String str = LocalDateTime.now().format(dateTimeFormatter) + " | " + text + "\n";
        try {
            out.write(str.getBytes());
            out.flush();
        } catch (IOException e) {
            logger.error("chat log write error", e);
        }
    }

    @Override
    public void dispose() {
        try {
            out.close();
        } catch (IOException e) {
            logger.error("error closing chat log");
        }
        super.dispose();
    }
}
