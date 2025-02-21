package client;

import protocol.messages.*;

public interface GameClient {

    void createGameMessage(int size);

    void register(RegisterMessage registerMessage);

    void queueUpdate(QueueUpdateMessage queueUpdateMessage);

    void gameStarted(GameStartingMessage gameStartingMessage);

    void onErrorMessage(ErrorMessage message);

    void onGameStateUpdate(GameStateUpdateMessage message);

    void logout();

    void joinQueue();

    void leaveQueue();

    void joinGame(int sessionCode);

    void leaveGame();
}
