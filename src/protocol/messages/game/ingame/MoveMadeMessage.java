package protocol.messages.game.ingame;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class MoveMadeMessage extends Message {

    public static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new MoveMadeMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public MoveMadeMessage(GameState gameState) {
        super(MessageType.MOVE_MADE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
