package protocol.messages.game.building;

import protocol.Ship;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;

/**
 * This message is sent from the client to the server when the player updates their ship placement.
 * It contains the ships that are currently placed on the build board.
 * This Message is sent during the Building phase of the game.
 * A response to this message from the server will be a:
 * - MoveMadeMessage; which contains the updated game state and will be broadcast to all players in the game.
 * or
 * - ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS); if the player is not in a game.
 * - ErrorMessage(ErrorType.INVALID_PLACEMENT); if the placement is invalid.
 */

public class PlayerUpdateShipPlacement extends Message {

    private final ArrayList<Ship> ships;

    /**
     * Creates a new PlayerUpdateShipPlacement instance.
     * @param ships The ships to be displayed on the build board.
     */
    public PlayerUpdateShipPlacement(ArrayList<Ship> ships) {
        super(MessageType.PLAYER_UPDATE_SHIP_PLACEMENT);
        this.ships = ships;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }
}
