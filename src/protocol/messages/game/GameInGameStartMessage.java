package protocol.messages.game;

import protocol.GameState;
import protocol.Ship;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;

public class GameInGameStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private GameState gameState;
    private ArrayList<Ship> yourShips;

    /**
     * Creates a new GameInGameStartMessage instance.
     * @param gameState The game state of the game that has started.
     * @param yourShips The ships of the player in the game.
     */
    public GameInGameStartMessage(GameState gameState, ArrayList<Ship> yourShips) {
        super(MessageType.GAME_IN_GAME_STARTS);
        this.gameState = gameState;
        this.yourShips = new ArrayList<>(yourShips);
    }

    public GameState getGameState() {
        return gameState;
    }

    public ArrayList<Ship> getYourShips() {
        return yourShips;
    }
}
