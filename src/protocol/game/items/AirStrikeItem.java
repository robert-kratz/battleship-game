package protocol.game.items;

import protocol.game.Item;
import protocol.game.ItemType;

public class AirStrikeItem extends Item {

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private final Orientation orientation;

    public AirStrikeItem(Orientation orientation) {
        super(ItemType.AIRSTRIKE, 60);

        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
