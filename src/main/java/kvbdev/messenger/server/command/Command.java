package kvbdev.messenger.server.command;

import kvbdev.messenger.server.Connection;

import java.util.function.BiConsumer;

public interface Command extends BiConsumer<Connection, String> {
    String getName();

    @Override
    void accept(Connection connection, String param);
}
