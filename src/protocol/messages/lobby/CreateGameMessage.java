package protocol.messages.lobby;

import protocol.GameOptions;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class CreateGameMessage extends Message {

    private GameOptions gameOptions;

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
