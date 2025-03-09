package protocol.messages.game.building;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class BuildReadyStateChange extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    public BuildReadyStateChange(GameState gameState) {
        super(MessageType.BUILD_READY_STATE_CHANGE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
