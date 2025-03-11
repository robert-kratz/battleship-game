package protocol.game.items;

import protocol.game.Item;
import protocol.game.ItemType;
import server.Parameters;

import java.io.Serializable;

public class RadarItem extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor for RadarItem.
     */
    public RadarItem() {
        super(ItemType.RADAR, Parameters.ITEM_RADAR_COST);
    }
}
