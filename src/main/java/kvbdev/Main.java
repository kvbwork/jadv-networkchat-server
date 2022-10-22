package kvbdev;

import kvbdev.messenger.server.MessengerServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws IOException {

        Properties properties = readPropertiesFromFile("server.properties");
        int port = Integer.parseInt(properties.getProperty("port", String.valueOf(13444)));

        MessengerServer messengerServer = new MessengerServer(port);
        messengerServer.start();

    }

    protected static Properties readPropertiesFromFile(String filename) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(filename)) {
            properties.load(in);
        }
        return properties;
    }

}
