package protocol.messages;

import server.GameState;

public class GameStateUpdateMessage extends Message {

    private static final long serialVersionUID = 1L;
    private GameState gameState;

    public GameStateUpdateMessage(GameState gameState) {
        super(MessageType.GAME_STATE_UPDATE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
