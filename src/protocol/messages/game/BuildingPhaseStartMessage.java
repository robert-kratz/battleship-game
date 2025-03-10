package protocol.messages.game;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class BuildingPhaseStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private GameState gameState;

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
