package client;

import protocol.Ship;
import protocol.messages.*;

import java.util.ArrayList;

public interface GameClient {

    void createGameMessage(int size);

    void register(RegisterMessage registerMessage);

    void queueUpdate(QueueUpdateMessage queueUpdateMessage);

    void gameStarted(GameStartingMessage gameStartingMessage);

    void onErrorMessage(ErrorMessage message);

    void onGameStateUpdate(GameStateUpdateMessage message);

    void placeShips(ArrayList<Ship> ships);

    void joinQueue();

    void leaveQueue();

    void joinGame(int sessionCode);

    void leaveGame();
}
