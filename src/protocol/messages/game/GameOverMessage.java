package protocol.messages.game;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the GameOverMessage which is sent from the server to the client when the game is over.
 * Either because:
 * - A player has won the game
 * - The game has ended in a draw
 * - A player has left / disconnected from the game
 *
 * This message is broadcast to all players in the game.
 */

public class GameOverMessage extends Message {

    public static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new GameOverMessage instance.
     * @param gameState The game state of the game that has ended.
     */
    public GameOverMessage(GameState gameState) {
        super(MessageType.GAME_OVER);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String toString() {
        return "GameOverMessage{" +
                "gameState=" + gameState +
                '}';
    }
}
