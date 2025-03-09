package protocol.messages;

import protocol.GameState;

public class MoveMadeMessage extends Message {

    public static final long serialVersionUID = 1L;

    private final GameState gameState;

    public MoveMadeMessage(GameState gameState) {
        super(MessageType.MOVE_MADE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
