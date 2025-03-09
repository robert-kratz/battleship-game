package server;

import protocol.GameOverReason;
import protocol.GameState;
import protocol.Ship;
import protocol.game.Move;

import java.util.ArrayList;

public interface Game {

    void addPlayer(PlayerInfo player);

    void removePlayer(PlayerInfo player);

    void onPlayerReadyStateChange(PlayerInfo player, boolean ready);

    void onPlayerPlaceShips(PlayerInfo player, ArrayList<Ship> ships);

    void onPlayerAttemptMove(PlayerInfo player, Move move);

    void sendGameStartingEvent();

    void sendInGameStartEvent();

    void sendTurnChangeEvent();

    void sendGameOverEvent(GameOverReason reason);

    GameState getGameState();

    ArrayList<Ship> getAvailableShips();

    void initAvailableShips();

    void setGameState(GameState gameState);
}
