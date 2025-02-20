package server;

import java.util.UUID;

public interface Game {

    GameState createGame(PlayerInfo playerA, int size);

    GameState createGame(PlayerInfo playerA, PlayerInfo playerB, int size);

    UUID joinGame(PlayerInfo playerB, int sessionCode);

    GameState leaveGame(PlayerInfo player);

    GameState getGameState();

    void setGameState(GameState gameState);
}
