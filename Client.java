// === Client.java ===
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final int PORT = 12345;
    private static final String HOST = "localhost";

    private JFrame frame;
    private JButton[][] buttons = new JButton[3][3];
    private JLabel statusLabel;
    private int playerId;
    private int currentTurn;
    private PrintWriter out;

    public Client() throws IOException {
        Socket socket = new Socket(HOST, PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // UI
        frame = new JFrame("Morpion");
        frame.setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(3, 3));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = i, y = j;
                buttons[i][j] = new JButton(" ");
                buttons[i][j].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
                buttons[i][j].addActionListener(e -> sendPlay(x, y));
                gridPanel.add(buttons[i][j]);
            }
        }

        statusLabel = new JLabel("Connexion...");
        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(gridPanel, BorderLayout.CENTER);

        JButton restartButton = new JButton("Rejouer");
        restartButton.addActionListener(e -> out.println("RESTART"));
        frame.add(restartButton, BorderLayout.SOUTH);

        frame.setSize(300, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Thread de lecture
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("CONNECTED")) {
                        playerId = Integer.parseInt(line.split(":")[1]);
                        frame.setTitle("Morpion - Joueur " + (playerId == 0 ? "X" : "O"));
                    } else if (line.startsWith("STATE")) {
                        char[][] grid = new char[3][3];
                        for (int i = 0; i < 3; i++) {
                            grid[i] = in.readLine().toCharArray();
                        }
                        String turnLine = in.readLine();
                        currentTurn = Integer.parseInt(turnLine.split(":")[1]);
                        updateBoard(grid);
                    } else if (line.startsWith("WIN")) {
                        int winner = Integer.parseInt(line.split(":")[1]);
                        JOptionPane.showMessageDialog(frame, "Le joueur " + (winner == 0 ? "X" : "O") + " a gagné !");
                    } else if (line.equals("DRAW")) {
                        JOptionPane.showMessageDialog(frame, "Match nul !");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendPlay(int x, int y) {
        if (playerId == currentTurn) {
            out.println("PLAY:" + x + ":" + y);
        }
    }

    private void updateBoard(char[][] grid) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText(grid[i][j] == '-' ? " " : String.valueOf(grid[i][j]));
                buttons[i][j].setEnabled(grid[i][j] == '-' && currentTurn == playerId);
            }
        }
        statusLabel.setText("C'est le tour du joueur " + (currentTurn == 0 ? "X" : "O"));
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}