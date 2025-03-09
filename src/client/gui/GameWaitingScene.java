package client.gui;

import client.GameHandler;
import client.Stage;
import client.StageManager;

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
    private int gameCode;

    // Neues Label für den Countdown-Timer
    private final JLabel timerLabel;
    // Timer für den Countdown
    private Timer countdownTimer;

    public GameWaitingScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.gameCode = gameHandler.getGameState().getSessionCode();

        this.userName = gameHandler.getClientHandler().getUsername();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 300));

        // Überschrift mit Game-Code
        // Hier fügen wir den MouseListener zum Kopieren hinzu
        JLabel codeLabel = new JLabel("Game Code: " + gameCode);
        codeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // MouseListener zum Kopieren des Game-Codes in die Zwischenablage
        codeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Game-Code in die Zwischenablage kopieren
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

        // "vs" Label in Fettschrift
        JLabel vsLabel = new JLabel("vs");
        vsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        vsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Waiting Label: Animation oder Gegnername
        waitingLabel = new JLabel("Waiting for players");
        waitingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Timer-Animation der Punkte, wenn opponentName null ist
        animationTimer = new Timer(500, e -> {
            if (opponentName == null) {
                dotCount = (dotCount + 1) % 4; // 0 bis 3 Punkte
                String dots = ".".repeat(dotCount);
                waitingLabel.setText("Waiting for players" + dots);
            }
        });
        animationTimer.start();

        // Neues Timer-Label (initial leer)
        timerLabel = new JLabel("");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Back-Button (volle Breite)
        JButton backButton = getBackButton(gameHandler, animationTimer);

        // Komponenten hinzufügen
        add(codeLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(modeLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(userNameLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(vsLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(waitingLabel);
        // Timer-Label unter dem Gegnernamen hinzufügen
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(timerLabel);
        add(Box.createVerticalGlue());
        add(backButton);

        // Falls der User einem bestehenden Spiel beitritt, setze den opponentName
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
            // Stoppe auch den Countdown, falls er läuft
            gameHandler.sendLeaveGame();
            gameHandler.getClientHandler().endCurrentGame(); // Beim Verlassen des Wartebildschirms das Spiel beenden
        });
        return backButton;
    }

    public void stopAnimation() {
        animationTimer.stop();
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
        if (opponentName != null) {
            waitingLabel.setText(opponentName);
            waitingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            animationTimer.stop();
        }
    }

    /**
     * Setzt den Startzeitpunkt des Spiels und startet den Countdown.
     * @param gameStartTime Das Datum, zu dem das Spiel startet.
     */
    public void setGameStartTime(Date gameStartTime) {
        // Vorherigen Countdown stoppen, falls vorhanden
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        // Timer aktualisiert den Countdown jede Sekunde
        countdownTimer = new Timer(1000, e -> {
            long diff = gameStartTime.getTime() - System.currentTimeMillis();
            if (diff <= 0) {
                timerLabel.setText("00:00");
                countdownTimer.stop();

                this.gameHandler.showBuildPhase(); // Starte die Build-Phase
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