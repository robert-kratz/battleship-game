package protocol;

import java.io.Serializable;
import java.util.ArrayList;

public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private int height, width;
    private String[][] board = new String[height][width];
    private ArrayList<Ship> ships = new ArrayList<>();

    public Board(int height, int width) {
        this.height = height;
        this.width = width;

        initShips();
    }

    public Board(String[][] board, ArrayList<Ship> ships) {
        this.board = board;
        this.ships = ships;

        //throw error when board is not a rectangle and or empty
        if (board.length == 0 || board[0].length == 0) {
            throw new IllegalArgumentException("Board is empty");
        }

        this.height = board.length;
        this.width = board[0].length;
    }

    private void initShips() {
        ships.add(new Ship(1, Ship.Orientation.NORTH, 2, 1));
        ships.add(new Ship(2, Ship.Orientation.NORTH, 2, 2));
        ships.add(new Ship(3, Ship.Orientation.NORTH, 3, 1));
        ships.add(new Ship(4, Ship.Orientation.NORTH, 4, 1));
        ships.add(new Ship(5, Ship.Orientation.NORTH, 5, 1));
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }

    public String[][] getBoard() {
        return board;
    }
}
