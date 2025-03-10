package server;

import protocol.GameOverReason;
import protocol.GameState;
import protocol.Ship;
import protocol.game.Move;

import java.util.ArrayList;

public interface Game {

    /**
     * Adds a player to the game.
     * @param player The player to add.
     */
    void addPlayer(ServerPlayer player);

    /**
     * Removes a player from the game.
     * @param player The player to remove.
     */
    void removePlayer(ServerPlayer player);

    /**
     * Checks if the game is full.
     * @param player The player to check.
     * @param ready Whether the player is ready or not.
     */
    void onPlayerReadyStateChange(ServerPlayer player, boolean ready);

    /**
     * Handles the event when a player hovers over a tile.
     * @param player The player who hovered over the tile.
     * @param ships The ships that the player has.
     */
    void onPlayerPlaceShips(ServerPlayer player, ArrayList<Ship> ships);

    /**
     * Handles the event when a player attempts to make a move.
     * @param player The player who attempted to make a move.
     * @param move The move object containing the move.
     */
    void onPlayerAttemptMove(ServerPlayer player, Move move);

    /**
     * Handles the event when a player hovers over a tile.
     */
    void sendGameStartingEvent();

    /**
     * Handles the event when a player hovers over a tile.
     */
    void sendInGameStartEvent();

    /**
     * Handles the event when a player hovers over a tile.
     */
    void sendTurnChangeEvent();

    /**
     * Handles the event when a player hovers over a tile.
     * @param reason The reason for the game over.
     */
    void sendGameOverEvent(GameOverReason reason);

    /**
     * Initializes the available ships for the game.
     */
    void initAvailableShips();

    GameState getGameState();

    void setGameState(GameState gameState);
}
