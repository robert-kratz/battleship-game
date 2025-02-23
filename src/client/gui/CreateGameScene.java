package client.gui;

import client.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateGameScene extends JPanel implements Scene {

    private final ButtonGroup sizeGroup;
    private final JRadioButton button8x8;
    private final JRadioButton button10x10;
    private final JRadioButton button12x12;

    private GameHandler gameHandler;

    @Override
    public String getTitle() {
        return "Create Game";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, 220);
    }

    public CreateGameScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding um das Panel
        setPreferredSize(new Dimension(320, 220));

        // Titel-Label
        JLabel titleLabel = new JLabel("Select your boardsize");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ButtonGroup für die Board-Größen
        sizeGroup = new ButtonGroup();
        button8x8 = new JRadioButton("8x8");
        button10x10 = new JRadioButton("10x10");
        button12x12 = new JRadioButton("12x12");

        // Zentrierung der Radiobuttons
        JPanel sizePanel = new JPanel(new GridLayout(1, 3, 10, 0));
        sizePanel.add(button8x8);
        sizePanel.add(button10x10);
        sizePanel.add(button12x12);

        sizeGroup.add(button8x8);
        sizeGroup.add(button10x10);
        sizeGroup.add(button12x12);

        sizePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create-Button (grün mit voller Breite)
        JButton createButton = new JButton("Create");
        createButton.setFocusPainted(false);
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Volle Breite

        // Back-Button (volle Breite)
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Volle Breite

        // Listener für Create-Button
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sizeGroup.getSelection() == null) {
                    JOptionPane.showMessageDialog(
                            CreateGameScene.this,
                            "Please select a board size.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    int selectedSize = getSelectedBoardSize();
                    System.out.println("Selected board size: " + selectedSize);

                    gameHandler.createGameMessage(selectedSize);
                }
            }
        });

        // Listener für Back-Button
        backButton.addActionListener(e -> {
            this.gameHandler.getStageManager().switchScene(Stage.LOBBY_SCENE);
            sizeGroup.clearSelection();
        });

        // Button-Panel (Create + Back vertikal angeordnet, volle Breite)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(createButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Abstand zwischen Create und Back
        buttonPanel.add(backButton);

        // Komponenten zum Hauptpanel hinzufügen
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 20))); // Abstand zum Size-Panel
        add(sizePanel);
        add(Box.createRigidArea(new Dimension(0, 30))); // Abstand zu den Buttons
        add(buttonPanel);
    }

    /**
     * Returns the selected board size as a string.
     */
    private int getSelectedBoardSize() {
        if (button8x8.isSelected()) return 8;
        if (button10x10.isSelected()) return 10;
        if (button12x12.isSelected()) return 12;
        return 10;
    }
}