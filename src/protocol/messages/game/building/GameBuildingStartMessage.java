package protocol.messages.game.building;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the GameBuildingStartMessage which is sent from the server to the client when the building phase starts.
 * It contains the game state of the game that has started.
 * It will be sent from the server to the client to confirm that the building phase has started.
 */

public class GameBuildingStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new GameBuildingStartMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public GameBuildingStartMessage(GameState gameState) {
        super(MessageType.BUILDING_PHASE_STARTS);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String toString() {
        return "GameBuildingStartMessage{" +
                "gameState=" + gameState +
                '}';
    }
}
