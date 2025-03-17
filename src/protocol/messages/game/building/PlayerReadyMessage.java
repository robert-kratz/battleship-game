package protocol.messages.game.building;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This message is sent from the client to the server when a player is ready.
 * A response to this message from the server will be:
 * - PlayerReadyStateChangeMessage
 * or
 * - ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS); if the player is not in a game.
 */

public class PlayerReadyMessage extends Message {

    private static final long serialVersionUID = 1L;

    public final boolean ready; // 0.5P Für Deklaration + Initialisierung + Gettet Methode

    /**
     * Creates a new PlayerReadyMessage instance.
     * @param ready True if the player is ready, false otherwise.
     */
    public PlayerReadyMessage(boolean ready) {
        super(MessageType.PLAYER_READY); // 0.5P Für Übergabe an Message
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public String toString() {
        return "PlayerReadyMessage{" +
                "ready=" + ready +
                '}';
    }
}
