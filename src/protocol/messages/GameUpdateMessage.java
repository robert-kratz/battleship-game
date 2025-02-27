package protocol.messages;

import protocol.GameState;
import protocol.Ship;

import java.util.ArrayList;

public class GameUpdateMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;
    private final ArrayList<Ship> ships;

    public GameUpdateMessage(GameState gameState, ArrayList<Ship> ships) {
        super(MessageType.GAME_UPDATE);
        this.gameState = gameState;
        this.ships = new ArrayList<>(ships);
    }

    public GameState getGameState() {
        return gameState;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }
}
