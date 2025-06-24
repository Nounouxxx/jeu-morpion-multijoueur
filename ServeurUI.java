import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class ServeurUI extends JFrame {
    private JLabel[][] cells = new JLabel[3][3];
    private JLabel infoLabel;
    private JButton stopButton;

    public ServeurUI() {
        super("Interface Serveur - Morpion");
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(3, 3));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = new JLabel("-", SwingConstants.CENTER);
                cells[i][j].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                gridPanel.add(cells[i][j]);
            }
        }

        infoLabel = new JLabel("En attente de parties...", SwingConstants.CENTER);
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        stopButton = new JButton("Arrêter le serveur");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Serveur.shutdown();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(infoLabel, BorderLayout.CENTER);
        bottomPanel.add(stopButton, BorderLayout.EAST);

        add(gridPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(350, 400);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Gestion fermeture fenêtre => arrêter proprement serveur
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                Serveur.shutdown();
            }
        });
    }

    public void updateGrid(char[][] grid, int currentPlayer, boolean gameEnded, int winner) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j].setText(grid[i][j] == '-' ? " " : String.valueOf(grid[i][j]));
            }
        }

        if (gameEnded) {
            if (winner == -1) {
                infoLabel.setText("Match nul !");
            } else {
                infoLabel.setText("Victoire du joueur " + (winner == 0 ? "X" : "O"));
            }
        } else {
            infoLabel.setText("Tour du joueur " + (currentPlayer == 0 ? "X" : "O"));
        }
    }

    public void dispose() {
        super.dispose();
    }
}
