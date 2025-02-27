package protocol.messages;

import protocol.GameState;

public class JoinGameMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    public JoinGameMessage(GameState gameState) {
        super(MessageType.JOIN_GAME);

        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
