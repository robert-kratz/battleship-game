package protocol;

import protocol.game.Cell;
import protocol.game.Move;
import server.Parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class ClientPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private boolean ready;
    private ArrayList<Move> moves;
    private ArrayList<Ship> uncoveredShips;
    private int energy;
    private boolean isInGame = true; //Indicates if the player has left the game
    private boolean turn = false;
    private boolean winner = false;

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

    public ClientPlayer(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.ready = false;
        this.moves = new ArrayList<>();
        this.uncoveredShips = new ArrayList<>();
    }

    public ArrayList<Cell> getAttackedCellsForPlayer() {
        ArrayList<Cell> attackedCells = new ArrayList<>();
        for (Move move : moves) {
            // If you want all cells (hits and misses), simply add them all:
            attackedCells.addAll(move.getAffectedCells());
        }
        return attackedCells;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setUncoveredShips(ArrayList<Ship> uncoveredShips) {
        this.uncoveredShips = uncoveredShips;
    }
}