package client;

import protocol.GameState;
import protocol.Ship;
import protocol.game.Hit;
import protocol.messages.*;

import java.util.ArrayList;

/**
 * Important naming convention: The methods in this interface should be named as follows:
 * - on<Name of the message>(<Message class name> <variable name>) -> For messages arriving from the Server to the Client.
 * - send<Name of the message>(<parameters>) -> For messages sent to the Server from the Client.
 */

public interface GameClient {

    //SERVER -> CLIENT

    /**
     * This method is called when:
     * - Times change
     * - Game State change
     * - Player State change
     * @param gameStateUpdateMessage The message containing the updated game state
     */
    void onGameStateUpdate(GameStateUpdateMessage gameStateUpdateMessage);

    /**
     * This method is called when the opponent hovers a tile
     * @param playerHoverMessage The message containing the player's hover information
     */
    void onPlayerHoverEvent(PlayerHoverMessage playerHoverMessage);

    /**
     * This method is called when:
     * - Move from opponent
     * Every action in the game regarding game logic
     * @param gameState The updated game state
     * @param yourShips The ships of the player
     */
    void onGameUpdate(GameState gameState, ArrayList<Ship> yourShips);

    /**
     * This method is called when an error occurs in the game
     * @param message The error message
     */
    void onGameError(ErrorMessage message);

    //CLIENT -> SERVER

    /**
     * If it is the users turn, this method is called when the user hovers a tile
     * @param x The x coordinate of the tile
     * @param y The y coordinate of the tile
     */
    void sendPlayerHoverEvent(int x, int y);

    /**
     * This submits a placement of ships to the server, each time the client places a ship
     * @param ships The ships to be placed
     */
    void sendSubmitPlacementEvent(ArrayList<Ship> ships);

    /**
     * This method is called when the user makes a move
     * @param hit The hit object containing the move
     */
    void sendGameMoveEvent(Hit hit);

    /**
     * This method is called when the user leaves the game, either by closing the window or pressing a button
     */
    void sendLeaveGame();

}
