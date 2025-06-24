import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class Serveur {
    private static final int PORT = 12345;
    private static final int GRID_SIZE = 3;
    private static final char EMPTY = '-';
    private static final char[] SYMBOLS = {'X', 'O'};

    private static char[][] grid = new char[GRID_SIZE][GRID_SIZE];
    // IMPORT CORRIGÉ : List et ArrayList déclarés explicitement
    private static List<ClientHandler> clients = new ArrayList<>();
    private static int currentPlayer = 0;
    private static boolean gameEnded = false;

    private static ServeurUI serverUI;
    private static volatile boolean serverRunning = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(PORT);
        initGrid();
        serverUI = new ServeurUI();
        updateUI();
        System.out.println("Serveur en attente de 2 joueurs...");

        // Thread pour accepter les connexions
        new Thread(() -> {
            try {
                while (serverRunning && clients.size() < 2) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, clients.size());
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                } else {
                    System.out.println("ServerSocket fermé, arrêt de l'acceptation.");
                }
            }
        }).start();
    }

    public static synchronized void initGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = EMPTY;
            }
        }
        currentPlayer = 0;
        gameEnded = false;
        updateUI();
    }

    public static synchronized boolean play(int x, int y, int player) {
        if (gameEnded || player != currentPlayer || x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE || grid[x][y] != EMPTY)
            return false;

        grid[x][y] = SYMBOLS[player];
        if (checkWin(SYMBOLS[player])) {
            gameEnded = true;
            broadcastWin(player);
        } else if (checkDraw()) {
            gameEnded = true;
            broadcastDraw();
        } else {
            currentPlayer = 1 - currentPlayer;
            broadcastState();
        }
        updateUI();
        return true;
    }

    private static boolean checkWin(char symbol) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if ((grid[i][0] == symbol && grid[i][1] == symbol && grid[i][2] == symbol) ||
                (grid[0][i] == symbol && grid[1][i] == symbol && grid[2][i] == symbol)) {
                return true;
            }
        }
        return (grid[0][0] == symbol && grid[1][1] == symbol && grid[2][2] == symbol) ||
               (grid[0][2] == symbol && grid[1][1] == symbol && grid[2][0] == symbol);
    }

    private static boolean checkDraw() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == EMPTY) return false;
            }
        }
        return true;
    }

    public static synchronized void broadcastState() {
        for (ClientHandler client : clients) {
            client.sendState(grid, currentPlayer);
        }
        updateUI();
    }

    public static synchronized void broadcastWin(int player) {
        for (ClientHandler client : clients) {
            client.sendMessage("WIN:" + player);
        }
        updateUI();
    }

    public static synchronized void broadcastDraw() {
        for (ClientHandler client : clients) {
            client.sendMessage("DRAW");
        }
        updateUI();
    }

    private static void updateUI() {
        if (serverUI != null) {
            int winner = -1;
            if (gameEnded && checkWin(SYMBOLS[0])) winner = 0;
            else if (gameEnded && checkWin(SYMBOLS[1])) winner = 1;
            serverUI.updateGrid(grid, currentPlayer, gameEnded, winner);
        }
    }

    public static void stopServer() {
        serverRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        System.out.println("Arrêt du serveur et des clients...");
        stopServer();
        for (ClientHandler client : clients) {
            client.close();
        }
        System.out.println("Serveur arrêté.");
        if (serverUI != null) {
            serverUI.dispose();
        }
        System.exit(0);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private int playerId;
        private PrintWriter out;
        private BufferedReader in;
        private volatile boolean running = true;

        public ClientHandler(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void sendState(char[][] grid, int currentPlayer) {
            StringBuilder sb = new StringBuilder("STATE\n");
            for (char[] row : grid) {
                sb.append(new String(row)).append("\n");
            }
            sb.append("TURN:").append(currentPlayer);
            out.println(sb);
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public void close() {
            running = false;
            try {
                if (socket != null && !socket.isClosed()) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            out.println("CONNECTED:" + playerId);
            broadcastState();

            try {
                String inputLine;
                while (running && (inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("PLAY")) {
                        String[] parts = inputLine.split(":");
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        play(x, y, playerId);
                    } else if (inputLine.equals("RESTART")) {
                        if (playerId == 0) {
                            initGrid();
                            broadcastState();
                        }
                    }
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            } finally {
                close();
            }
        }
    }
}
