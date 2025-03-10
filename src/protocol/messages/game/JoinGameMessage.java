package protocol.messages.game;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the JoinGameMessage which is sent from the server to the client when a player joins a game.
 * It contains the game state of the game that the player has joined.
 * It will be sent from the server to the client to confirm that the player has joined the game.
 */

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
