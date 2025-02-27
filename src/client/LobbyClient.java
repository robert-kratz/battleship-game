package client;

import protocol.messages.ErrorMessage;
import protocol.messages.QueueUpdateMessage;

/**
 * Important naming convention: The methods in this interface should be named as follows:
 * - on<Name of the message>(<Message class name> <variable name>) -> For messages arriving from the Server to the Client.
 * - send<Name of the message>(<parameters>) -> For messages sent to the Server from the Client.
 */

public interface LobbyClient {

    //SERVER -> CLIENT

    /**
     * This method is called when the queue is updated.
     * @param queueUpdateMessage The message containing the queue information.
     */
    void onQueueUpdate(QueueUpdateMessage queueUpdateMessage);

    /**
     * This method is called when an error occurs in the lobby.
     * @param errorMessage The error message.
     */
    void onLobbyError(ErrorMessage errorMessage);

    //CLIENT -> SERVER

    /**
     * Join the queue to find a game.
     */
    void sendJoinQueueEvent();

    /**
     * Leave the queue.
     */
    void sendLeaveQueueEvent();

    /**
     * Create a game.
     * @param size The size of the game.
     */
    void sendCreateGameEvent(int size);

    /**
     * Join a game with a code.
     * @param code The code of the game.
     */
    void sendJoinGameWithCodeEvent(int code);
}
