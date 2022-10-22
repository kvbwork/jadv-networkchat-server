package kvbdev.messenger.server;

public interface ConnectionInputHandler {

    boolean handle(String text, Connection connection);

}
