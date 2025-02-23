package server;

import java.util.UUID;

public interface Game {

    GameState addPlayer(PlayerInfo player);

    GameState leaveGame(PlayerInfo player);

    GameState getGameState();

    void setGameState(GameState gameState);
}
