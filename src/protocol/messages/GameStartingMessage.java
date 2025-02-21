package protocol.messages;

import server.GameState;

public class GameStartingMessage extends Message {

    private static final long serialVersionUID = 1L;

    private GameState gameState;

    public GameStartingMessage(GameState gameState) {
        super(MessageType.GAME_STARTING);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
