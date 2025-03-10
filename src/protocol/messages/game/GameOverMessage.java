package protocol.messages.game;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class GameOverMessage extends Message {

    public static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new GameOverMessage instance.
     * @param gameState The game state of the game that has ended.
     */
    public GameOverMessage(GameState gameState) {
        super(MessageType.GAME_END);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
