package protocol;

import protocol.game.Move;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class ClientPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String name;
    private boolean ready;
    private ArrayList<Move> moves;
    private ArrayList<Ship> uncoveredShips;
    private int energy;
    private boolean isInGame = true; //Indicates if the player has left the game
    private boolean turn = false;
    private boolean winner = false;

    /**
     * Copy constructor for ClientPlayer.
     * @param clientPlayer the ClientPlayer to copy
     */
    public ClientPlayer(ClientPlayer clientPlayer) {
        this.id = clientPlayer.id;
        this.name = clientPlayer.name;
        this.ready = clientPlayer.ready;
        this.moves = new ArrayList<>(clientPlayer.moves);
        this.uncoveredShips = new ArrayList<>(clientPlayer.uncoveredShips);
        this.energy = clientPlayer.energy;
        this.isInGame = clientPlayer.isInGame;
        this.turn = clientPlayer.turn;
        this.winner = clientPlayer.winner;
    }

    /**
     * Constructor for ClientPlayer.
     * @param id the UUID of the player
     * @param name the name of the player
     */
    public ClientPlayer(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.ready = false;
        this.moves = new ArrayList<>();
        this.uncoveredShips = new ArrayList<>();
    }

    public boolean isPlayer(UUID id) {
        return this.id.equals(id);
    }

    public boolean isTurn() {
        return turn;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public ArrayList<Ship> getUncoveredShips() {
        return uncoveredShips;
    }

    public int getEnergy() {
        return energy;
    }

    public UUID getId() {
        return id;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public void setInGame(boolean inGame) {
        isInGame = inGame;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void addEnergy(int energy) {
        this.energy += energy;
    }

    public void removeEnergy(int energy) {
        this.energy -= energy;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setUncoveredShips(ArrayList<Ship> uncoveredShips) {
        this.uncoveredShips = uncoveredShips;
    }
}