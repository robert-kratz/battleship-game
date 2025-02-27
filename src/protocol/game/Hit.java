package protocol.game;

import protocol.game.items.AirStrikeItem;
import protocol.game.items.RadarItem;
import protocol.game.items.SeaBombItem;

public class Hit {

    private final int x, y;
    private boolean isHit = false;

    public Hit(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Hit(SeaBombItem seaBombItem, int seaBombX, int seaBombY) {
        this.x = seaBombX;
        this.y = seaBombY;
    }

    public Hit(AirStrikeItem airStrikeItem, int airStrikeX, int airStrikeY) {
        this.x = airStrikeX;
        this.y = airStrikeY;
    }

    public Hit(RadarItem radarItem, int radarX, int radarY) {
        this.x = radarX;
        this.y = radarY;
    }

    public void setHit(boolean hit) {
        this.isHit = hit;
    }

    public boolean isHit() {
        return isHit;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
