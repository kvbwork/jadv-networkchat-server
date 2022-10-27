package kvbdev.messenger.server.command;

import kvbdev.messenger.server.Connection;

public class EchoCommand extends AbstractCommand {

    public EchoCommand() {
        super("/echo");
    }

    @Override
    public void accept(Connection connection, String param) {
        if (param == null || param.isEmpty()) return;
        connection.writeLine(param);
    }
}
