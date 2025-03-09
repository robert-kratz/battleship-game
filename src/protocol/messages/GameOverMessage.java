package protocol.messages;

import protocol.GameState;

public class GameOverMessage extends Message {

    public static final long serialVersionUID = 1L;

    private final GameState gameState;

    public GameOverMessage(GameState gameState) {
        super(MessageType.GAME_END);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
