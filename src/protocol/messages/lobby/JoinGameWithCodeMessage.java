package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class JoinGameWithCodeMessage extends Message {

    private int sessionCode;

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
