package protocol.messages.game;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class JoinGameMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new JoinGameMessage instance.
     * @param gameState The game state of the game to join.
     */
    public JoinGameMessage(GameState gameState) {
        super(MessageType.JOIN_GAME);

        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
