package protocol;

import server.Parameters;

import java.io.Serializable;

public class GameOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private int BOARD_SIZE;

    private int BUILD_TIME;
    private int MOVE_TIME;
    private int MOVE_HIT_TIME;

    private int ENERGY_GAME_START;
    private int ENERGY_TURN_BONUS;
    private int ENERGY_SHIP_HIT;

    /**
     * Default constructor for GameOptions.
     */
    public GameOptions () {
        this.BUILD_TIME = Parameters.DEFAULT_BUILD_TIME;
        this.MOVE_TIME = Parameters.DEFAULT_MOVE_TIME;
        this.MOVE_HIT_TIME = Parameters.DEFAULT_MOVE_HIT_TIME;

        this.ENERGY_GAME_START = Parameters.DEFAULT_ENERGY_GAME_START;
        this.ENERGY_TURN_BONUS = Parameters.DEFAULT_ENERGY_TURN_BONUS;
        this.ENERGY_SHIP_HIT = Parameters.DEFAULT_ENERGY_SHIP_HIT;
        this.BOARD_SIZE = 10;
    }

    /**
     * Constructor for GameOptions with specified parameters.
     * @param BUILD_TIME the time to build a ship
     */
    public void setBuildTime(int BUILD_TIME) {
        this.BUILD_TIME = BUILD_TIME;
    }

    /**
     * Sets the time to move a ship.
     * @param MOVE_TIME the time to move a ship
     */
    public void setMoveTime(int MOVE_TIME) {
        this.MOVE_TIME = MOVE_TIME;
    }

    /**
     * Sets the time bonus for moving and hitting.
     * @param MOVE_HIT_TME_BONUS the time bonus for moving and hitting
     */
    public void setMoveHitTimeBonus(int MOVE_HIT_TME_BONUS) {
        this.MOVE_HIT_TIME = MOVE_HIT_TME_BONUS;
    }

    /**
     * Sets the energy cost for starting the game.
     * @param ENERGY_GAME_START the energy cost for starting the game
     */
    public void setEnergyGameStart(int ENERGY_GAME_START) {
        this.ENERGY_GAME_START = ENERGY_GAME_START;
    }

    /**
     * Sets the energy bonus for each turn.
     * @param ENERGY_TURN_BONUS the energy bonus for each turn
     */
    public void setEnergyTurnBonus(int ENERGY_TURN_BONUS) {
        this.ENERGY_TURN_BONUS = ENERGY_TURN_BONUS;
    }

    /**
     * Sets the size of the game board.
     * @param BOARD_SIZE the size of the game board
     */
    public void setBoardSize(int BOARD_SIZE) {
        this.BOARD_SIZE = BOARD_SIZE;
    }

    /**
     * Sets the energy cost for hitting a ship.
     * @param ENERGY_SHIP_HIT the energy cost for hitting a ship
     */
    public void setEnergyShipHit(int ENERGY_SHIP_HIT) {
        this.ENERGY_SHIP_HIT = ENERGY_SHIP_HIT;
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public int getBuildTime() {
        return BUILD_TIME;
    }

    public int getMoveTime() {
        return MOVE_TIME;
    }

    public int getMoveHitTimeBonus() {
        return MOVE_HIT_TIME;
    }

    public int getEnergyGameStart() {
        return ENERGY_GAME_START;
    }

    public int getEnergyTurnBonus() {
        return ENERGY_TURN_BONUS;
    }

    public int getEnergyShipHit() {
        return ENERGY_SHIP_HIT;
    }
}
