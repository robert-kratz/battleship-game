package protocol;

import protocol.game.Cell;
import protocol.game.Move;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public class MoveManager {

    private final GameState gameState;

    public MoveManager(GameState gameState) {
        this.gameState = gameState;
    }

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
                if (m.getAirStrikeItem().getOrientation() == move.getAirStrikeItem().getOrientation()) {
                    if (m.getX() == move.getX() && m.getY() == move.getY()) {
                        System.out.println("Already placed an air strike item on this row/column");
                        return false;
                    }
                }
            }
        } else {
            // Für einen normalen Hit: Sammle alle bereits betroffenen Zellen.
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
     * Prüft, ob auf dem gesamten Spielbrett noch mindestens eine Zelle übrig ist,
     * die noch nicht getroffen wurde.
     *
     * @return true, wenn es mindestens eine ungetroffene Zelle gibt, ansonsten false.
     */
    public boolean isAMoveStillPossible() {
        int boardSize = gameState.getBoardSize();
        // Erstelle ein 2D-Array, um zu markieren, welche Zellen getroffen wurden.
        boolean[][] hitCells = new boolean[boardSize][boardSize];

        // Markiere alle betroffenen Zellen aus den Zügen von Spieler A.
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
        // Markiere alle betroffenen Zellen aus den Zügen von Spieler B.
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
        // Überprüfe, ob es mindestens eine Zelle gibt, die noch nicht getroffen wurde.
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (!hitCells[x][y]) {
                    return true;
                }
            }
        }
        return false;
    }
}