package protocol.messages.game.ingame;

import protocol.GameState;
import protocol.Ship;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;

/**
 * This is the GameInGameStartMessage which is sent from the server to the client when a game starts.
 * This message is in response either to a PlayerReadyMessage; If both players are ready, the game starts (Triggered by this message);
 * Or to the build time running out (Triggered by the GameBuildingStartMessage).
 */

public class GameInGameStartMessage extends Message {

    private static final long serialVersionUID = 1L;

    private GameState gameState; // 0.5P Für Deklaration + Initialisierung + Gettet Methode
    private ArrayList<Ship> yourShips; // 0.5P Für Deklaration + Initialisierung + Gettet Methode

    /**
     * Creates a new GameInGameStartMessage instance.
     * @param gameState The game state of the game that has started.
     * @param yourShips The ships of the player in the game.
     */
    public GameInGameStartMessage(GameState gameState, ArrayList<Ship> yourShips) { 
        super(MessageType.GAME_IN_GAME_START); // 0.5P Für Übergabe an Message
        this.gameState = gameState;
        this.yourShips = new ArrayList<>(yourShips);
    }

    public GameState getGameState() {
        return gameState;
    }

    public ArrayList<Ship> getYourShips() {
        return yourShips;
    }

    @Override
    public String toString() {
        return "GameInGameStartMessage{" +
                "gameState=" + gameState +
                ", yourShips=" + yourShips +
                '}';
    }
}
