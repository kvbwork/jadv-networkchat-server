package kvbdev.messenger.server.command;

import kvbdev.messenger.server.Connection;

public class ExitCommand extends AbstractCommand {

    public ExitCommand() {
        super("/exit");
    }

    @Override
    public void accept(Connection connection, String param) {
        if (!connection.isClosed()) connection.close();
    }
}
