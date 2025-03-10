package protocol.messages.game;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the BuildingPhaseStartMessage which is sent from the server to the client when the building phase starts.
 * It contains the game state of the game that has started.
 * It will be sent from the server to the client to confirm that the building phase has started.
 */

public class BuildingPhaseStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new BuildingPhaseStartMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public BuildingPhaseStartMessage(GameState gameState) {
        super(MessageType.BUILDING_PHASE_STARTS);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
