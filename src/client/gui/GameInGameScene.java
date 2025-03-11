package client.gui;

import client.ClientHandler;
import client.GameHandler;
import client.SoundType;
import client.gui.board.InGameBattleshipBoard;
import client.gui.painter.BoardPainter;
import client.gui.painter.InGameBoardPainter;
import protocol.ClientPlayer;
import protocol.MoveManager;
import protocol.Ship;
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
import java.util.Map;

public class GameInGameScene extends JPanel implements Runnable {

    private GameHandler gameHandler;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel timerLabel;

    public InGameBattleshipBoard playerBoard, opponentBoard;

    private boolean playersTurn; // true = Players turn, false = Opponents turn

    private Date currentTurnStart;
    private Date currentTurnEnd;
    private JLabel playerTurnLabel;
    private JLabel opponentTurnLabel;

    private JLabel energyLabel;
    private JButton bombButton;
    private JButton radarButton;
    private JButton airStrikeButton;
    private JButton giveUpButton;

    /**
     * Returns the size of the window.
     * @return The size of the window as a Dimension object.
     */
    public Dimension getWindowSize() {
        return new Dimension(900, 500);
    }

    /**
     * Creates a new GameInGameScene with the specified game handler and placed ships.
     * @param gameHandler The game handler to be used for managing the game state.
     * @param placedShips The list of ships that are placed on the board.
     */
    public GameInGameScene(GameHandler gameHandler, ArrayList<Ship> placedShips) {
        this.gameHandler = gameHandler;

        setPreferredSize(getWindowSize());

        this.playersTurn = gameHandler.getGameState().isPlayersTurn(this.gameHandler.getClientHandler().getUserId());
        this.currentTurnStart = this.gameHandler.getGameState().getPlayersTurnStart();
        this.currentTurnEnd = this.gameHandler.getGameState().getPlayersTurnEnd();

        // Board-Painter for InGame Phase
        Image inGameBackgroundImage = new ImageIcon("resource/background-2.png").getImage();
        BoardPainter inGamePainter = new InGameBoardPainter(inGameBackgroundImage);

        // Create the player board
        playerBoard = new InGameBattleshipBoard(
                gameHandler.getGameState().getBoardSize(),
                placedShips,
                inGamePainter,
                new InGameBattleshipBoard.InGameBoardListener() {
                    @Override
                    public void onCellClick(int row, int col) {
                        // No action needed here
                    }

                    @Override
                    public void onHover(int row, int col, ArrayList<protocol.game.Cell> affectedCells) {
                        // No action needed here
                    }

                    @Override
                    public void onSelectedItemChanged(Item newItem) {
                        updateItemButtonHighlighting(newItem);
                    }
                }
        );

        // Create the opponent board
        opponentBoard = new InGameBattleshipBoard(
                gameHandler.getGameState().getBoardSize(),
                this.gameHandler.getGameState().getUncoveredShips(this.gameHandler.getClientHandler().getUserId()),
                inGamePainter,
                new InGameBattleshipBoard.InGameBoardListener() {
                    @Override
                    public void onCellClick(int row, int col) {
                        ClientPlayer player = gameHandler.getGameState().getPlayer(gameHandler.getClientHandler().getUserId());

                        Move move = getMove(row, col);
                        MoveManager moveManager = new MoveManager(gameHandler.getGameState());

                        move.computeAffectedCells(gameHandler.getGameState().getBoardSize());

                        if(moveManager.isPlayerMoveMoveValid(player.getId(), move)) {
                            opponentBoard.setSelectedItem(null);
                            gameHandler.sendGameMoveEvent(move);
                        } else {
                            System.err.println("Invalid move: " + move);
                            //JOptionPane.showMessageDialog(null, "Invalid move!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    private Move getMove(int row, int col) {
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
                        return move;
                    }

                    @Override
                    public void onHover(int row, int col, ArrayList<protocol.game.Cell> affectedCells) {
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

        // Energy
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

            if(opponentBoard.getSelectedItem() instanceof SeaBombItem) {
                removeSelectedItems();
            } else {
                opponentBoard.setSelectedItem(new SeaBombItem());
                updateItemButtonHighlighting(opponentBoard.getSelectedItem());
                opponentBoard.requestFocusInWindow();
            }
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

            if(opponentBoard.getSelectedItem() instanceof RadarItem) {
                removeSelectedItems();
            } else {
                System.out.println("Radar item selected");
                opponentBoard.setSelectedItem(new RadarItem());
                updateItemButtonHighlighting(opponentBoard.getSelectedItem());
                opponentBoard.requestFocusInWindow();
            }
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

            if(opponentBoard.getSelectedItem() instanceof AirStrikeItem) {
                removeSelectedItems();
            } else {
                System.out.println("Air Strike item selected");
                opponentBoard.setSelectedItem(new AirStrikeItem(AirStrikeItem.Orientation.HORIZONTAL));
                updateItemButtonHighlighting(opponentBoard.getSelectedItem());
                opponentBoard.requestFocusInWindow();
            }
        });
        leftPanel.add(airStrikeButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Activate/Deactivate Item Buttons for given energy
        updateItemButtons(energy);

        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Timer
        timerLabel = new JLabel("00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(timerLabel);

        // Right Panel
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

        // Show the right board depending on the player's turn
        if (playersTurn) {
            opponentBoard.setInteractive(true);
            playerBoard.setInteractive(false);
        } else {
            opponentBoard.setInteractive(false);
            playerBoard.setInteractive(false);
        }
    }

    /**
     * Clears the selected items on both boards.
     */
    public void removeSelectedItems() {
        playerBoard.clearSelectedItem();
        opponentBoard.clearSelectedItem();
    }

    /**
     * Updates the item buttons based on the player's energy.
     * @param energy The current energy of the player.
     */
    private void updateItemButtons(int energy) {
        boolean canSelect = playersTurn;
        bombButton.setEnabled(canSelect && energy >= new SeaBombItem().getEnergyCost());
        radarButton.setEnabled(canSelect && energy >= new RadarItem().getEnergyCost());
        airStrikeButton.setEnabled(canSelect && energy >= new AirStrikeItem().getEnergyCost());
    }

    /**
     * Updates the highlighting of the item buttons based on the selected item.
     * @param selected The selected item.
     */
    private void updateItemButtonHighlighting(Item selected) {

        bombButton.setForeground(Color.BLACK);
        radarButton.setForeground(Color.BLACK);
        airStrikeButton.setForeground(Color.BLACK);

        if (selected instanceof SeaBombItem) {
            bombButton.setForeground(Color.ORANGE);
            radarButton.setForeground(Color.GRAY);
            airStrikeButton.setForeground(Color.GRAY);
            bombButton.setOpaque(false);
        } else if (selected instanceof RadarItem) {
            radarButton.setForeground(Color.ORANGE);
            bombButton.setForeground(Color.GRAY);
            airStrikeButton.setForeground(Color.GRAY);
            bombButton.setOpaque(false);
        } else if (selected instanceof AirStrikeItem) {
            airStrikeButton.setForeground(Color.ORANGE);
            bombButton.setForeground(Color.GRAY);
            radarButton.setForeground(Color.GRAY);
            bombButton.setOpaque(false);
        }
    }

    /**
     * Sets the game handler for this scene.
     * @param newEndTime The new end time for the current turn.
     */
    public void extendCurrentTurn(Date newEndTime) {
        this.currentTurnEnd = newEndTime;
    }

    /**
     * Toggles the turn between the player and the opponent.
     * @param isPlayersTurn true if it's the player's turn, false if it's the opponent's turn.
     */
    public void toggleTurn(boolean isPlayersTurn) {
        this.playersTurn = isPlayersTurn;
        playerTurnLabel.setText(isPlayersTurn ? "Your Turn" : "Waiting for Opponent");
        opponentTurnLabel.setText(!isPlayersTurn ? "Opponents Turn" : "Waiting for your Move");
        playerTurnLabel.setForeground(isPlayersTurn ? Color.GREEN : Color.LIGHT_GRAY);
        opponentTurnLabel.setForeground(!isPlayersTurn ? Color.GREEN : Color.LIGHT_GRAY);

        // Reset the selected items and affected cells
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

        // Toggle the interactivity of the boards
        if (playersTurn) {
            opponentBoard.setInteractive(true);
            playerBoard.setInteractive(false);
        } else {
            opponentBoard.setInteractive(false);
            playerBoard.setInteractive(false);
        }
    }

    /**
     * Sets the hover effect for the opponent's board.
     * @param row the target row
     * @param col the target column
     * @param affectedCells the list of affected cells
     */
    public void setOpponentHover(int row, int col, ArrayList<Cell> affectedCells) {
        if (affectedCells == null) {
            affectedCells = new ArrayList<>();
        }
        if (!playersTurn) {
            playerBoard.setOpponentAffectedCells(affectedCells);
        }
    }

    /**
     * Sets the hover effect for the player's board.
     * @param energy the current energy of the player
     */
    public void setPlayerEnergy(int energy) {
        energyLabel.setText("Energy: " + energy);
        updateItemButtons(energy);
    }

    /**
     * Sets the hover effect for the opponent's board.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000); // Call every second
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
            } else {
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                SwingUtilities.invokeLater(() -> timerLabel.setText(timeStr));
            }
        }
    }
}