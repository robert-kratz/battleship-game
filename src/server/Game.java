package server;

import protocol.GameState;

public interface Game {

    GameState addPlayer(PlayerInfo player);

    GameState leaveGame(PlayerInfo player);

    GameState getGameState();

    void setGameState(GameState gameState);
}
