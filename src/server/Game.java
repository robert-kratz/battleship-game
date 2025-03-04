package server;

import protocol.GameState;
import protocol.game.Move;

public interface Game {



    void addPlayer(PlayerInfo player);

    GameState leaveGame(PlayerInfo player);

    void playerMove(PlayerInfo player, Move move);

    GameState getGameState();

    void setGameState(GameState gameState);
}
