package protocol.messages.game.ingame;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class PlayerTurnChangeMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    public PlayerTurnChangeMessage(GameState gameState) {
        super(MessageType.TURN_CHANGE);

        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
