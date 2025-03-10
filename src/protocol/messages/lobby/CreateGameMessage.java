package protocol.messages.lobby;

import protocol.GameOptions;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the CreateGameMessage which is sent from the client to the server when a player wants to create a new game.
 * An argument to this message is the game options; which contains the game mode;
 * An response to this message from the server will be a
 * - JoinGameMessage; for the game that has started.
 * or
 * - ErrorMessage(ErrorType.INVALID_GAME_SIZE); if the game size is invalid.
 */

public class CreateGameMessage extends Message {

    private final GameOptions gameOptions;

    /**
     * Creates a new CreateGameMessage instance.
     * @param gameOptions The game options for the new game.
     */
    public CreateGameMessage(GameOptions gameOptions) {
        super(MessageType.CREATE_GAME);

        this.gameOptions = gameOptions;
    }

    public GameOptions getGameOptions() {
        return gameOptions;
    }
}
