package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This message is sent from the client to the server when a player wants to join a game using a session code.
 * The session code is the six-digit code that is used to join a game.
 * A response to this message from the server will be a:
 * - JoinGameMessage; for the game that has started.
 * or
 * - ErrorMessage(ErrorType.INVALID_SESSION_CODE); if the session code is invalid.
 * - ErrorMessage(ErrorType.GAME_ALREADY_STARTED); if the player is already in a game.
 * - ErrorMessage(ErrorType.ALREADY_IN_GAME); if the game is not found.
 */

public class JoinGameWithCodeMessage extends Message {

    private final int sessionCode;

    /**
     * Creates a new JoinGameWithCodeMessage instance.
     * @param sessionCode The session code to join the game.
     */
    public JoinGameWithCodeMessage(int sessionCode) {
        super(MessageType.JOIN_GAME_WITH_CODE);

        this.sessionCode = sessionCode;

    }

    public int getSessionCode() {
        return sessionCode;
    }
}
