package client.gui;

import client.ClientHandler;
import client.GameHandler;
import client.SoundType;
import client.gui.board.InGameBattleshipBoard;
import client.gui.painter.BoardPainter;
import client.gui.painter.InGameBoardPainter;
import protocol.GameState;
import protocol.Ship;
import protocol.ShipPlacementValidator;
import protocol.game.Cell;
import protocol.game.Move;
import protocol.game.items.AirStrikeItem;
import protocol.game.items.RadarItem;
import protocol.game.items.SeaBombItem;
import protocol.game.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

public class GameInGameScene extends JPanel implements Runnable {

    private GameHandler gameHandler;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel timerLabel;
    public InGameBattleshipBoard playerBoard, opponentBoard;
    private boolean playersTurn;
    private Date currentTurnStart;
    private Date currentTurnEnd;
    private JLabel playerTurnLabel;
    private JLabel opponentTurnLabel;
    // Felder für Energieanzeige und Item-Buttons
    private JLabel energyLabel;
    private JButton bombButton;
    private JButton radarButton;
    private JButton airStrikeButton;
    private JButton giveUpButton;

    public GameInGameScene(GameHandler gameHandler, ArrayList<Ship> placedShips) {
        this.gameHandler = gameHandler;
        this.playersTurn = gameHandler.getGameState().isPlayersTurn(this.gameHandler.getClientHandler().getUserId());
        this.currentTurnStart = this.gameHandler.getGameState().getPlayersTurnStart();
        this.currentTurnEnd = this.gameHandler.getGameState().getPlayersTurnEnd();

        // Board-Painter für InGame-Phase
        Image inGameBackgroundImage = new ImageIcon("resource/background-2.png").getImage();
        BoardPainter inGamePainter = new InGameBoardPainter(inGameBackgroundImage);

        System.out.println("GameInGameScene created");
        for (Ship ship : placedShips) {
            System.out.println("Ship: " + ship);
        }

        // Erzeuge beide Boards mit erweitertem onHover-Event und Callback, wenn sich das selektierte Item ändert.
        playerBoard = new InGameBattleshipBoard(
                gameHandler.getGameState().getBoardSize(),
                placedShips,
                inGamePainter,
                new InGameBattleshipBoard.InGameBoardListener() {
                    @Override
                    public void onCellClick(int row, int col) {
                        System.out.println("playerBoard clicked at: " + row + ", " + col);
                    }

                    @Override
                    public void onHover(int row, int col, ArrayList<protocol.game.Cell> affectedCells) {
                        System.out.println("playerBoard hovered at: " + row + ", " + col + ". Affected cells: " + affectedCells);
                    }

                    @Override
                    public void onSelectedItemChanged(Item newItem) {
                        updateItemButtonHighlighting(newItem);
                    }
                }
        );

        opponentBoard = new InGameBattleshipBoard(
                gameHandler.getGameState().getBoardSize(),
                this.gameHandler.getGameState().getUncoveredShips(this.gameHandler.getClientHandler().getUserId()),
                inGamePainter,
                new InGameBattleshipBoard.InGameBoardListener() {
                    @Override
                    public void onCellClick(int row, int col) {
                        System.out.println("opponentBoard clicked at: " + row + ", " + col);

                        ArrayList<Cell> takenCells;

                        if(gameHandler.getGameState().getPlayerA().isPlayer(gameHandler.getClientHandler().getUserId())) {
                            takenCells = gameHandler.getGameState().getAttackedCellsForPlayerA();
                        } else {
                            takenCells = gameHandler.getGameState().getAttackedCellsForPlayerB();
                        }

                        //ceck if the cell is already taken
                        for (Cell cell : takenCells) {
                            if (cell.getX() == row && cell.getY() == col) {
                                System.out.println("Cell already taken");
                                return;
                            }
                        }

                        Move move;

                        if(opponentBoard.getSelectedItem() != null) {
                            move = switch (opponentBoard.getSelectedItem().getClass().getSimpleName()) {
                                case "SeaBombItem" -> {
                                    SeaBombItem seaBombItem = (SeaBombItem) opponentBoard.getSelectedItem();

                                    yield new Move(seaBombItem, col, row);
                                }
                                case "RadarItem" -> {
                                    RadarItem radarItem = (RadarItem) opponentBoard.getSelectedItem();

                                    yield new Move(radarItem, col, row);
                                }
                                case "AirStrikeItem" -> {
                                    AirStrikeItem airStrikeItem = (AirStrikeItem) opponentBoard.getSelectedItem();

                                    yield new Move(airStrikeItem, airStrikeItem.getOrientation() == AirStrikeItem.Orientation.HORIZONTAL ? row : col);
                                }
                                default -> new Move(col, row);
                            };
                        } else {
                            move = new Move(col, row);
                        }

                        System.out.println("Sending move: " + move);

                        gameHandler.sendGameMoveEvent(move);
                    }

                    @Override
                    public void onHover(int row, int col, ArrayList<protocol.game.Cell> affectedCells) {
                        System.out.println("opponentBoard hovered at: " + row + ", " + col + ". Affected cells: " + affectedCells);
                        // Sende das Hover-Event an den Server
                        gameHandler.sendPlayerHoverEvent(row, col, affectedCells);
                    }

                    @Override
                    public void onSelectedItemChanged(Item newItem) {
                        updateItemButtonHighlighting(newItem);
                    }
                }
        );

        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Titel
        JLabel title = new JLabel("Battleships");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Energieanzeige
        int energy = this.gameHandler.getGameState().getPlayer(this.gameHandler.getClientHandler().getUserId()).getEnergy();
        energyLabel = new JLabel("Energy: " + energy);
        energyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        energyLabel.setForeground(Color.WHITE);
        energyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        leftPanel.add(energyLabel);
        leftPanel.add(Box.createVerticalStrut(10));

        // Item-Buttons

        // Bomb (SeaBombItem)
        bombButton = new JButton("Bombs (2x2): " + new SeaBombItem().getEnergyCost() + " Energy");
        bombButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bombButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bombButton.setBackground(Color.WHITE);
        bombButton.addActionListener(e -> {
            if (!playersTurn) return;

            removeSelectedItems(); // Entferne selektierte Items, da AirStrike nur einmalig ausgewählt werden kann

            System.out.println("Bomb item selected");
            opponentBoard.setSelectedItem(new SeaBombItem());
            updateItemButtonHighlighting(opponentBoard.getSelectedItem());
            opponentBoard.requestFocusInWindow();
        });
        leftPanel.add(bombButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Radar (RadarItem)
        radarButton = new JButton("Radar: " + new RadarItem().getEnergyCost() + " Energy");
        radarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        radarButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        radarButton.setBackground(Color.WHITE);
        radarButton.addActionListener(e -> {
            if (!playersTurn) return;

            removeSelectedItems(); // Entferne selektierte Items, da AirStrike nur einmalig ausgewählt werden kann

            System.out.println("Radar item selected");
            opponentBoard.setSelectedItem(new RadarItem());
            updateItemButtonHighlighting(opponentBoard.getSelectedItem());
            opponentBoard.requestFocusInWindow();
        });
        leftPanel.add(radarButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Air Strike (AirStrikeItem)
        airStrikeButton = new JButton("Air Strike: " + new AirStrikeItem().getEnergyCost() + " Energy");
        airStrikeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        airStrikeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        airStrikeButton.setBackground(Color.WHITE);
        airStrikeButton.addActionListener(e -> {
            if (!playersTurn) return;

            removeSelectedItems(); // Entferne selektierte Items, da AirStrike nur einmalig ausgewählt werden kann


            System.out.println("Air Strike item selected");
            opponentBoard.setSelectedItem(new AirStrikeItem());
            updateItemButtonHighlighting(opponentBoard.getSelectedItem());
            opponentBoard.requestFocusInWindow();
        });
        leftPanel.add(airStrikeButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Aktivierung der Buttons abhängig von der aktuellen Energie
        updateItemButtons(energy);

        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Timer-Anzeige
        timerLabel = new JLabel("00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(timerLabel);

        // Rechtes Panel (Spieler- und Gegnerinfos)
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.DARK_GRAY);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel yourLabel = new JLabel("You:");
        yourLabel.setForeground(Color.WHITE);
        yourLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        yourLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(yourLabel);

        JLabel usernameLabel = new JLabel(gameHandler.getClientHandler().getUsername());
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(usernameLabel);

        playerTurnLabel = new JLabel(this.playersTurn ? "Your Turn" : "Waiting for Opponent");
        playerTurnLabel.setForeground(this.playersTurn ? Color.GREEN : Color.LIGHT_GRAY);
        playerTurnLabel.setFont(new Font("Arial", Font.BOLD, 12));
        playerTurnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(playerTurnLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        JLabel opponentLabel = new JLabel("Your Opponent:");
        opponentLabel.setForeground(Color.WHITE);
        opponentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        opponentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentLabel);

        String opponentName = gameHandler.getGameState().getOpponent(this.gameHandler.getClientHandler().getUserId()).getName();

        JLabel opponentNameLabel = new JLabel(opponentName);
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        opponentNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentNameLabel);

        opponentTurnLabel = new JLabel(!this.playersTurn ? "Opponents Turn" : "Waiting for your Move");
        opponentTurnLabel.setForeground(!this.playersTurn ? Color.GREEN : Color.LIGHT_GRAY);
        opponentTurnLabel.setFont(new Font("Arial", Font.BOLD, 12));
        opponentTurnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentTurnLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        giveUpButton = new JButton("Give Up");
        giveUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        giveUpButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        giveUpButton.setBackground(Color.RED);
        giveUpButton.setOpaque(true);
        giveUpButton.addActionListener(e -> {
            this.gameHandler.sendLeaveGame();
            this.gameHandler.getClientHandler().endCurrentGame();
        });
        rightPanel.add(giveUpButton);

        setLayout(new BorderLayout());
        add(leftPanel, BorderLayout.WEST);
        add(playersTurn ? opponentBoard : playerBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Nur das aktive Board interaktiv schalten:
        if (playersTurn) {
            opponentBoard.setInteractive(true);
            playerBoard.setInteractive(false);
        } else {
            opponentBoard.setInteractive(false);
            playerBoard.setInteractive(false);
        }
    }

    public void removeSelectedItems() {
        playerBoard.clearSelectedItem();
        opponentBoard.clearSelectedItem();
    }

    private void updateItemButtons(int energy) {
        boolean canSelect = playersTurn;
        bombButton.setEnabled(canSelect && energy >= new SeaBombItem().getEnergyCost());
        radarButton.setEnabled(canSelect && energy >= new RadarItem().getEnergyCost());
        airStrikeButton.setEnabled(canSelect && energy >= new AirStrikeItem().getEnergyCost());
    }

    private void updateItemButtonHighlighting(Item selected) {
        // Setze alle Buttons zunächst auf Weiß
        bombButton.setBackground(Color.WHITE);
        radarButton.setBackground(Color.WHITE);
        airStrikeButton.setBackground(Color.WHITE);

        if (selected == null) return;
        if (selected instanceof SeaBombItem) {
            bombButton.setBackground(Color.YELLOW);
        } else if (selected instanceof RadarItem) {
            radarButton.setBackground(Color.YELLOW);
        } else if (selected instanceof AirStrikeItem) {
            airStrikeButton.setBackground(Color.YELLOW);
        }
    }

    public void extendCurrentTurn(Date newEndTime) {
        this.currentTurnEnd = newEndTime;
    }

    public void toggleTurn(boolean isPlayersTurn) {
        this.playersTurn = isPlayersTurn;
        playerTurnLabel.setText(isPlayersTurn ? "Your Turn" : "Waiting for Opponent");
        opponentTurnLabel.setText(!isPlayersTurn ? "Opponents Turn" : "Waiting for your Move");
        playerTurnLabel.setForeground(isPlayersTurn ? Color.GREEN : Color.LIGHT_GRAY);
        opponentTurnLabel.setForeground(!isPlayersTurn ? Color.GREEN : Color.LIGHT_GRAY);

        // Bei Turn-Wechsel die selektierten Items und Hover-Daten zurücksetzen
        playerBoard.clearSelectedItem();
        opponentBoard.clearSelectedItem();
        playerBoard.clearOpponentAffectedCells();
        opponentBoard.clearOpponentAffectedCells();

        this.gameHandler.getGameState().setPlayersTurnStart(this.gameHandler.getGameState().getPlayersTurnStart());
        this.gameHandler.getGameState().setPlayersTurnEnd(this.gameHandler.getGameState().getPlayersTurnEnd());

        remove(playersTurn ? playerBoard : opponentBoard);
        add(playersTurn ? opponentBoard : playerBoard, BorderLayout.CENTER);
        revalidate();
        repaint();

        // Interaktivität des Boards anpassen
        if (playersTurn) {
            opponentBoard.setInteractive(true);
            playerBoard.setInteractive(false);
        } else {
            opponentBoard.setInteractive(false);
            playerBoard.setInteractive(false);
        }
    }

    /**
     * Setzt die Gegner-Hover-Daten im eigenen Board (playerBoard).
     * Falls der übergebene Parameter null ist, wird stattdessen eine leere Liste gesetzt.
     */
    public void setOpponentHover(int row, int col, ArrayList<Cell> affectedCells) {
        if (affectedCells == null) {
            System.out.println("Affected cells are null. Setting to empty list.");
            affectedCells = new ArrayList<>();
        }
        // Wenn der Spieler sein eigenes Board sieht (playersTurn == false),
        // wird im playerBoard das Gegner-Hover-Overlay gesetzt.
        if (!playersTurn) {
            System.out.println("Setting opponent hover at: " + row + ", " + col + ". Affected cells: " + affectedCells);
            playerBoard.setOpponentAffectedCells(affectedCells);
        }
    }

    public void setPlayerEnergy(int energy) {
        energyLabel.setText("Energy: " + energy);
        updateItemButtons(energy);
    }

    @Override
    public void run() {
        System.out.println("GameInGameScene started");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000); // 1 Sekunde warten
            } catch (InterruptedException e) {
                break;
            }
            long remainingMillis = currentTurnEnd.getTime() - System.currentTimeMillis();
            long minutes = (remainingMillis / (60 * 1000)) % 60;
            long seconds = (remainingMillis / 1000) % 60;

            if (seconds == 3 || seconds == 2 || seconds == 1) {
                ClientHandler.mediaPlayer.playSound(SoundType.CLOCK_TICK);
            }

            if (remainingMillis <= 0) {
                SwingUtilities.invokeLater(() -> timerLabel.setText("Waiting for Server"));
                System.out.println("Timer expired. Executing expiration logic.");
            } else {
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                SwingUtilities.invokeLater(() -> timerLabel.setText(timeStr));
            }
        }
    }
}