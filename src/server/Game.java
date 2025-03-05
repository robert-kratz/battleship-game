package server;

import protocol.GameState;
import protocol.Ship;
import protocol.game.Move;

import java.util.ArrayList;

public interface Game {

    void addPlayer(PlayerInfo player);

    void leaveGame(PlayerInfo player);

    void playerMove(PlayerInfo player, Move move);

    void onPlayerReadyStateChange(PlayerInfo player, boolean ready);

    void onPlayerPlaceShips(PlayerInfo player, ArrayList<Ship> ships);

    GameState getGameState();

    void setGameState(GameState gameState);
}
