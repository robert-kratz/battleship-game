package protocol.messages.game.building;

import protocol.Ship;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;

public class UpdateBuildBoardMessage extends Message {

    private final ArrayList<Ship> ships;

    public UpdateBuildBoardMessage(ArrayList<Ship> ships) {
        super(MessageType.UPDATE_BUILD_BOARD);
        this.ships = ships;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }
}
