package protocol.messages;

import protocol.GameState;

public class BuildingPhaseStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private GameState gameState;

    public BuildingPhaseStartMessage(GameState gameState) {
        super(MessageType.BUILDING_PHASE_STARTS);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
