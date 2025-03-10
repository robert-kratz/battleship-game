package protocol.messages.game.building;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the BuildReadyStateChangeMessage which is sent from the server to the client when the player is ready.
 * This message is broadcast to all players in the game in response to a PlayerReadyMessage
 * to inform them that the player is ready.
 */

public class BuildReadyStateChangeMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new BuildReadyStateChangeMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public BuildReadyStateChangeMessage(GameState gameState) {
        super(MessageType.BUILD_READY_STATE_CHANGE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
