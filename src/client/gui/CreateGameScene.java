package client.gui;

import client.ClientHandler;
import client.Stage;
import protocol.GameOptions;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class CreateGameScene extends JPanel {

    // Main Title
    private final JLabel mainTitleLabel;

    private final JPanel boardSizePanel;
    private final TitledBorder boardSizeBorder;
    private final ButtonGroup sizeGroup;
    private final JRadioButton button8x8;
    private final JRadioButton button10x10;
    private final JRadioButton button12x12;

    private final JPanel buildTimePanel;
    private final TitledBorder buildTimeBorder;
    private final JSlider buildTimeSlider;

    private final JPanel moveTimePanel;
    private final TitledBorder moveTimeBorder;
    private final JSlider moveTimeSlider;

    private final JPanel bonusTimePanel;
    private final TitledBorder bonusTimeBorder;
    private final JSlider bonusTimeSlider;

    private final JPanel startEnergyPanel;
    private final TitledBorder startEnergyBorder;
    private final JSlider startEnergySlider;

    private final JPanel shipHitEnergyPanel;
    private final TitledBorder shipHitEnergyBorder;
    private final JSlider shipHitEnergySlider;

    private final JPanel energyTurnBonusPanel;
    private final TitledBorder energyTurnBonusBorder;
    private final JSlider energyTurnBonusSlider;

    private GameOptions gameOptions = new GameOptions();

    public Dimension getWindowSize() {
        return new Dimension(430, 730);
    }

    /**
     * Creates a new CreateGameScene with the specified client handler.
     * @param clientHandler The client handler to be used for communication.
     */
    public CreateGameScene(ClientHandler clientHandler) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(getWindowSize());

        // --- Main Title ---
        mainTitleLabel = new JLabel("Create Game");
        mainTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Board Size Panel ---
        boardSizePanel = new JPanel();
        boardSizePanel.setLayout(new BoxLayout(boardSizePanel, BoxLayout.Y_AXIS));
        boardSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Default: Board Size (10)
        boardSizeBorder = BorderFactory.createTitledBorder("Board Size (10x10)");
        boardSizePanel.setBorder(boardSizeBorder);

        sizeGroup = new ButtonGroup();
        button8x8 = new JRadioButton("8x8");
        button10x10 = new JRadioButton("10x10");
        button12x12 = new JRadioButton("12x12");

        // Default: 10x10
        button10x10.setSelected(true);

        ActionListener boardSizeListener = e -> {
            if (button8x8.isSelected()) {
                boardSizeBorder.setTitle("Board Size (8x8)");
                gameOptions.setBoardSize(8);
            } else if (button10x10.isSelected()) {
                boardSizeBorder.setTitle("Board Size (10x10)");
                gameOptions.setBoardSize(10);
            } else if (button12x12.isSelected()) {
                boardSizeBorder.setTitle("Board Size (12x12)");
                gameOptions.setBoardSize(12);
            }
            boardSizePanel.repaint();
        };
        button8x8.addActionListener(boardSizeListener);
        button10x10.addActionListener(boardSizeListener);
        button12x12.addActionListener(boardSizeListener);

        sizeGroup.add(button8x8);
        sizeGroup.add(button10x10);
        sizeGroup.add(button12x12);

        JPanel boardSizeRadioPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        boardSizeRadioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        boardSizeRadioPanel.add(button8x8);
        boardSizeRadioPanel.add(button10x10);
        boardSizeRadioPanel.add(button12x12);
        boardSizePanel.add(boardSizeRadioPanel);

        // --- Build Time Panel ---
        buildTimePanel = new JPanel();
        buildTimePanel.setLayout(new BoxLayout(buildTimePanel, BoxLayout.Y_AXIS));
        buildTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        buildTimeBorder = BorderFactory.createTitledBorder("Build Time (" + gameOptions.getBuildTime() + " seconds)");
        buildTimePanel.setBorder(buildTimeBorder);
        buildTimeSlider = new JSlider(JSlider.HORIZONTAL, 10, 60, gameOptions.getBuildTime());
        buildTimeSlider.setMajorTickSpacing(10);
        buildTimeSlider.setMinorTickSpacing(1);
        buildTimeSlider.setPaintTicks(true);
        buildTimeSlider.setPaintLabels(true);
        buildTimeSlider.addChangeListener(e -> {
            int value = buildTimeSlider.getValue();
            buildTimeBorder.setTitle("Build Time (" + value + " seconds)");
            gameOptions.setBuildTime(value);
            buildTimePanel.repaint();
        });
        buildTimePanel.add(buildTimeSlider);

        // --- Move Time Panel ---
        moveTimePanel = new JPanel();
        moveTimePanel.setLayout(new BoxLayout(moveTimePanel, BoxLayout.Y_AXIS));
        moveTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        moveTimeBorder = BorderFactory.createTitledBorder("Move Time (" + gameOptions.getMoveTime() + " seconds)");
        moveTimePanel.setBorder(moveTimeBorder);
        moveTimeSlider = new JSlider(JSlider.HORIZONTAL, 5, 30, gameOptions.getMoveTime());
        moveTimeSlider.setMajorTickSpacing(5);
        moveTimeSlider.setMinorTickSpacing(1);
        moveTimeSlider.setPaintTicks(true);
        moveTimeSlider.setPaintLabels(true);
        moveTimeSlider.addChangeListener(e -> {
            int value = moveTimeSlider.getValue();
            moveTimeBorder.setTitle("Move Time (" + value + " seconds)");
            gameOptions.setMoveTime(value);
            moveTimePanel.repaint();
        });
        moveTimePanel.add(moveTimeSlider);

        // --- Bonus Time Panel ---
        bonusTimePanel = new JPanel();
        bonusTimePanel.setLayout(new BoxLayout(bonusTimePanel, BoxLayout.Y_AXIS));
        bonusTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bonusTimeBorder = BorderFactory.createTitledBorder("Move Bonus Time (" + gameOptions.getMoveHitTimeBonus() + " seconds)");
        bonusTimePanel.setBorder(bonusTimeBorder);
        bonusTimeSlider = new JSlider(JSlider.HORIZONTAL, 0, 20, gameOptions.getMoveHitTimeBonus());
        bonusTimeSlider.setMajorTickSpacing(5);
        bonusTimeSlider.setMinorTickSpacing(1);
        bonusTimeSlider.setPaintTicks(true);
        bonusTimeSlider.setPaintLabels(true);
        bonusTimeSlider.addChangeListener(e -> {
            int value = bonusTimeSlider.getValue();
            bonusTimeBorder.setTitle("Move Bonus Time (" + value + " seconds)");
            gameOptions.setMoveHitTimeBonus(value);
            bonusTimePanel.repaint();
        });
        bonusTimePanel.add(bonusTimeSlider);

        // --- Start Energy Panel ---
        startEnergyPanel = new JPanel();
        startEnergyPanel.setLayout(new BoxLayout(startEnergyPanel, BoxLayout.Y_AXIS));
        startEnergyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        startEnergyBorder = BorderFactory.createTitledBorder("Start Energy (" + gameOptions.getEnergyGameStart() + " energy)");
        startEnergyPanel.setBorder(startEnergyBorder);
        startEnergySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, gameOptions.getEnergyGameStart());
        startEnergySlider.setMajorTickSpacing(20);
        startEnergySlider.setMinorTickSpacing(5);
        startEnergySlider.setPaintTicks(true);
        startEnergySlider.setPaintLabels(true);
        startEnergySlider.addChangeListener(e -> {
            int value = startEnergySlider.getValue();
            startEnergyBorder.setTitle("Start Energy (" + value + " energy)");
            gameOptions.setEnergyGameStart(value);
            startEnergyPanel.repaint();
        });
        startEnergyPanel.add(startEnergySlider);

        // --- Ship Hit Energy Panel ---
        shipHitEnergyPanel = new JPanel();
        shipHitEnergyPanel.setLayout(new BoxLayout(shipHitEnergyPanel, BoxLayout.Y_AXIS));
        shipHitEnergyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        shipHitEnergyBorder = BorderFactory.createTitledBorder("Ship Hit Energy (" + gameOptions.getEnergyShipHit() + " energy)");
        shipHitEnergyPanel.setBorder(shipHitEnergyBorder);
        shipHitEnergySlider = new JSlider(JSlider.HORIZONTAL, 0, 10, gameOptions.getEnergyShipHit());
        shipHitEnergySlider.setMajorTickSpacing(5);
        shipHitEnergySlider.setMinorTickSpacing(1);
        shipHitEnergySlider.setPaintTicks(true);
        shipHitEnergySlider.setPaintLabels(true);
        shipHitEnergySlider.addChangeListener(e -> {
            int value = shipHitEnergySlider.getValue();
            shipHitEnergyBorder.setTitle("Ship Hit Energy (" + value + " energy)");
            gameOptions.setEnergyShipHit(value);
            shipHitEnergyPanel.repaint();
        });
        shipHitEnergyPanel.add(shipHitEnergySlider);

        // --- Energy Turn Bonus Panel ---
        energyTurnBonusPanel = new JPanel();
        energyTurnBonusPanel.setLayout(new BoxLayout(energyTurnBonusPanel, BoxLayout.Y_AXIS));
        energyTurnBonusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        energyTurnBonusBorder = BorderFactory.createTitledBorder("Energy Turn Bonus (" + gameOptions.getEnergyTurnBonus() + " energy)");
        energyTurnBonusPanel.setBorder(energyTurnBonusBorder);
        energyTurnBonusSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, gameOptions.getEnergyTurnBonus());
        energyTurnBonusSlider.setMajorTickSpacing(5);
        energyTurnBonusSlider.setMinorTickSpacing(1);
        energyTurnBonusSlider.setPaintTicks(true);
        energyTurnBonusSlider.setPaintLabels(true);
        energyTurnBonusSlider.addChangeListener(e -> {
            int value = energyTurnBonusSlider.getValue();
            energyTurnBonusBorder.setTitle("Energy Turn Bonus (" + value + " energy)");
            gameOptions.setEnergyTurnBonus(value);
            energyTurnBonusPanel.repaint();
        });

        energyTurnBonusPanel.add(energyTurnBonusSlider);

        // --- Buttons ---
        JButton createButton = new JButton("Create");
        createButton.setFocusPainted(false);
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        createButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        createButton.addActionListener(e -> {
            if (sizeGroup.getSelection() == null) {
                JOptionPane.showMessageDialog(
                        CreateGameScene.this,
                        "Please select a board size.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                int selectedSize = getSelectedBoardSize();
                int buildTime = buildTimeSlider.getValue();
                int moveTime = moveTimeSlider.getValue();
                int bonusTime = bonusTimeSlider.getValue();
                int startEnergy = startEnergySlider.getValue();
                int shipHitEnergy = shipHitEnergySlider.getValue();

                System.out.println("Selected board size: " + selectedSize);
                System.out.println("Build Time: " + buildTime);
                System.out.println("Move Time: " + moveTime);
                System.out.println("Move Bonus Time: " + bonusTime);
                System.out.println("Start Energy: " + startEnergy);
                System.out.println("Ship Hit Energy: " + shipHitEnergy);

                clientHandler.getLobbyHandler().sendCreateGameEvent(gameOptions);

                resetValues();
            }
        });

        backButton.addActionListener(e -> {
            clientHandler.getStageManager().switchScene(Stage.LOBBY_SCENE);
            sizeGroup.clearSelection();
            resetValues();
        });

        add(mainTitleLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(boardSizePanel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(buildTimePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(moveTimePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(bonusTimePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(startEnergyPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(shipHitEnergyPanel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(energyTurnBonusPanel);
        add(Box.createRigidArea(new Dimension(0, 20)));

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(createButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(backButton);
        add(buttonPanel);
    }

    /**
     * Resets the values of the sliders and game options to their default values.
     */
    private void resetValues() {
        gameOptions = new GameOptions();
        buildTimeSlider.setValue(gameOptions.getBuildTime());
        moveTimeSlider.setValue(gameOptions.getMoveTime());
        bonusTimeSlider.setValue(gameOptions.getMoveHitTimeBonus());
        startEnergySlider.setValue(gameOptions.getEnergyGameStart());
        shipHitEnergySlider.setValue(gameOptions.getEnergyShipHit());
        energyTurnBonusSlider.setValue(gameOptions.getEnergyTurnBonus());
    }

    /**
     * Returns the selected board size based on the selected radio button.
     * @return The selected board size (8, 10, or 12).
     */
    private int getSelectedBoardSize() {
        if (button8x8.isSelected()) return 8;
        if (button10x10.isSelected()) return 10;
        if (button12x12.isSelected()) return 12;
        return 10;
    }
}