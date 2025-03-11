package client.gui;

import client.GameHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.UUID;

public class GameWaitingScene extends JPanel {

    private final JLabel waitingLabel;
    private final Timer animationTimer;
    private int dotCount = 0;

    private final GameHandler gameHandler;

    private String userName, opponentName;
    private final int gameCode;

    private final JLabel timerLabel;
    private Timer countdownTimer;

    /**
     * Returns the size of the window.
     * @return The size of the window as a Dimension object.
     */
    public Dimension getWindowSize() {
        return new Dimension(900, 500);
    }

    /**
     * Creates a new GameWaitingScene.
     * @param gameHandler The GameHandler instance used to manage the game state.
     */
    public GameWaitingScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.gameCode = gameHandler.getGameState().getSessionCode();

        this.userName = gameHandler.getClientHandler().getUsername();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(getWindowSize());

        JLabel codeLabel = new JLabel("Game Code: " + gameCode);
        codeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        codeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                StringSelection selection = new StringSelection(String.valueOf(gameCode));
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        });

        // Modus-Label (z. B. 8x8)
        JLabel modeLabel = new JLabel("Mode: " + gameHandler.getGameState().getBoardSize()
                + "x" + gameHandler.getGameState().getBoardSize());
        modeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username Label
        JLabel userNameLabel = new JLabel(userName);
        userNameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // "vs" Label
        JLabel vsLabel = new JLabel("vs");
        vsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        vsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Waiting Label: Animation or Opponent Name
        waitingLabel = new JLabel("Waiting for players");
        waitingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Timer-Animation of the pointless dots, if no opponent is found
        animationTimer = new Timer(500, e -> {
            if (opponentName == null) {
                dotCount = (dotCount + 1) % 4; // 0 bis 3 Punkte
                String dots = ".".repeat(dotCount);
                waitingLabel.setText("Waiting for players" + dots);
            }
        });
        animationTimer.start();

        // New Timer-Label
        timerLabel = new JLabel("");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Back-Button
        JButton backButton = getBackButton(gameHandler, animationTimer);

        // Add components to the panel
        add(codeLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(modeLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(userNameLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(vsLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(waitingLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(timerLabel);
        add(Box.createVerticalGlue());
        add(backButton);

        // If the game is 2-player, set the opponent name
        if(gameHandler.getGameState().getPlayerCount() == 2) {
            UUID userId = gameHandler.getClientHandler().getUserId();
            setOpponentName(gameHandler.getGameState().getOpponent(userId).getName());
        }
    }

    private static JButton getBackButton(GameHandler gameHandler, Timer animationTimer) {
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            animationTimer.stop();
            // kill countdownTimer
            gameHandler.sendLeaveGame();
            gameHandler.getClientHandler().endCurrentGame(); // End Game on client side
        });
        return backButton;
    }

    /**
     * Sets the name of the opponent.
     * @param opponentName The name of the opponent.
     */
    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
        if (opponentName != null) {
            waitingLabel.setText(opponentName);
            waitingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            animationTimer.stop();
        }
    }

    /**
     * Sets the game start time and starts the countdown timer.
     * @param gameStartTime The time when the game starts.
     */
    public void setGameStartTime(Date gameStartTime) {
        // Stop timer if it is already running
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        // Refresh the timer label
        countdownTimer = new Timer(1000, e -> {
            long diff = gameStartTime.getTime() - System.currentTimeMillis();
            if (diff <= 0) {
                timerLabel.setText("00:00");
                countdownTimer.stop();

                this.gameHandler.showBuildPhase(); // Start the build phase
            } else {
                int seconds = (int) (diff / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            }
        });
        countdownTimer.start();
    }
}