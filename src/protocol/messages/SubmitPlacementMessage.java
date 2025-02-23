package protocol.messages;

import protocol.Ship;

import java.util.ArrayList;

public class SubmitPlacementMessage extends Message {

    private final ArrayList<Ship> ships;

    public SubmitPlacementMessage(ArrayList<Ship> ships) {
        super(MessageType.SUBMIT_PLACEMENT);
        this.ships = ships;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }
}
