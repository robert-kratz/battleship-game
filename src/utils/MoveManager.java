package utils;

import protocol.GameState;
import protocol.Ship;
import protocol.game.Cell;
import protocol.game.Move;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public class MoveManager {

    private final GameState gameState;

    /**
     * Creates a new MoveManager with the given game state.
     * @param gameState The game state to be managed.
     */
    public MoveManager(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Checks if the given move is valid for the specified player.
     * @param player The UUID of the player.
     * @param move The move to be checked.
     * @return true if the move is valid, false otherwise.
     */
    public boolean isPlayerMoveMoveValid(UUID player, Move move) {
        // Check if the move is in bounds.
        if(move.getX() < 0 || move.getX() >= gameState.getBoardSize() ||
                move.getY() < 0 || move.getY() >= gameState.getBoardSize())
            return false;

        if(gameState.getPlayerA().isPlayer(player)) {
            ArrayList<Move> moves = gameState.getPlayerA().getMoves();
            return isMoveValid(move, moves);
        } else if(gameState.getPlayerB().isPlayer(player)) {
            ArrayList<Move> moves = gameState.getPlayerB().getMoves();
            return isMoveValid(move, moves);
        } else {
            return false;
        }
    }

    /**
     * Checks if the move is valid by checking if it has already been hit or if it is out of bounds.
     * @param move The move to be checked.
     * @param moves The list of moves already made.
     * @return true if the move is valid, false otherwise.
     */
    private boolean isMoveValid(Move move, ArrayList<Move> moves) {
        ArrayList<Cell> pointsAlreadyHit = new ArrayList<>();

        if(move.getRadarItem() != null) {
            // Only check if the target already has a radar item placed.
            for (Move m : moves) {
                if (m.getRadarItem() != null && m.getX() == move.getX() && m.getY() == move.getY()) {
                    System.out.println("Already placed a radar item on this point");
                    return false;
                }
            }
        } else if(move.getSeaBombItem() != null) {
            // Only check if the target already has a sea bomb item placed.
            for (Move m : moves) {
                if (m.getSeaBombItem() != null && m.getX() == move.getX() && m.getY() == move.getY()) {
                    System.out.println("Already placed a sea bomb item on this point");
                    return false;
                }
            }
            // Check if x,y is in bounds – because it is a 2x2 and x,y is the top left corner.
            if(move.getX() + 1 >= gameState.getBoardSize() || move.getY() + 1 >= gameState.getBoardSize()) {
                System.out.println("Sea bomb item out of bounds");
                return false;
            }
        } else if(move.getAirStrikeItem() != null) {
            // Only check if the target already has an air strike item placed.
            for (Move m : moves) {
                if (m.getAirStrikeItem() != null && m.getAirStrikeItem().getOrientation() == move.getAirStrikeItem().getOrientation()) {
                    if (m.getRowOrColumn() == move.getRowOrColumn()) {
                        System.out.println("Already placed an air strike item on this row/column");
                        return false;
                    }
                }
            }
        } else {
            // For a normal hit: Collect all already affected cells.
            for(Move m : moves) {
                pointsAlreadyHit.addAll(m.getAffectedCells());
            }
            for (Cell cell : move.getAffectedCells()) {
                for (Cell point : pointsAlreadyHit) {
                    if (point.getX() == cell.getX() && point.getY() == cell.getY()) {
                        System.out.println("Already hit this point");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Generates a random move for the specified player.
     * @param player The UUID of the player.
     * @return A valid random move.
     */
    public Move makeRandomMove(UUID player) {
        int x = (int) (Math.random() * gameState.getBoardSize());
        int y = (int) (Math.random() * gameState.getBoardSize());
        Move move = new Move(x, y);
        System.out.println("Attempting to make random move: " + move);
        while(!isPlayerMoveMoveValid(player, move)) {
            x = (int) (Math.random() * gameState.getBoardSize());
            y = (int) (Math.random() * gameState.getBoardSize());
            move = new Move(x, y);
        }
        System.out.println("Made random move: " + move);
        return move;
    }

    /**
     * Checks if a move is still possible.
     * @return true if a move is still possible, false otherwise.
     */
    public boolean isAMoveStillPossible() {
        int boardSize = gameState.getBoardSize();
        // create a 2D array to track hit cells
        boolean[][] hitCells = new boolean[boardSize][boardSize];

        // This is done to check if there are any cells that are not hit yet.
        for (Move move : gameState.getPlayerA().getMoves()){
            move.computeAffectedCells(boardSize);
            for (Cell cell : move.getAffectedCells()) {
                int x = cell.getX();
                int y = cell.getY();
                if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                    hitCells[x][y] = true;
                }
            }
        }
        // Check if there are any cells that are not hit yet.
        for (Move move : gameState.getPlayerB().getMoves()){
            move.computeAffectedCells(boardSize);
            for (Cell cell : move.getAffectedCells()) {
                int x = cell.getX();
                int y = cell.getY();
                if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                    hitCells[x][y] = true;
                }
            }
        }
        // Check weather there are any cells that are not hit yet.
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (!hitCells[x][y]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given move hits any of the ships.
     * @param ships The list of ships to check against.
     * @param move The move to be checked.
     * @return true if the move hits a ship, false otherwise.
     */
    public static boolean moveHasHit(ArrayList<Ship> ships, Move move) {
        // Ensure that affectedCells are computed before checking.
        // (If not already computed, you might want to call move.computeAffectedCells(boardSize) beforehand.)
        for (Cell cell : move.getAffectedCells()) {
            for (Ship ship : ships) {
                // Get the list of points (cells) occupied by the ship.
                ArrayList<Point> occupiedCells = ship.getOccupiedCells();
                for (Point p : occupiedCells) {
                    if (p.x == cell.getX() && p.y == cell.getY()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}