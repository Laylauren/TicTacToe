package gradlettt.com.lauren.tictactoe.tictactoe;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

class Player {
    String name;
    String symbol;
    int wins;
    int losses;

    Player(String name, String symbol, int wins, int losses) {
        this.name = name;
        this.symbol = symbol;
        this.wins = wins;
        this.losses = losses;
    }
}

class GameBoard {
    JButton[][] board;
    int size = 3;

    GameBoard() {
        board = new JButton[size][size];
    }

    // Reset the board for a new game
    void resetBoard() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                board[r][c].setText("");
                board[r][c].setBackground(Color.white);
                board[r][c].setForeground(Color.darkGray);
                board[r][c].setEnabled(true);
            }
        }
    }

    // Returns array of winning buttons, or null if no winner
    JButton[] getWinningLine(String symbol) {
        // Horizontal
        for (int r = 0; r < size; r++) {
            if (equalsSymbol(board[r][0], symbol) &&
                equalsSymbol(board[r][1], symbol) &&
                equalsSymbol(board[r][2], symbol)) {
                return new JButton[]{board[r][0], board[r][1], board[r][2]};
            }
        }

        // Vertical
        for (int c = 0; c < size; c++) {
            if (equalsSymbol(board[0][c], symbol) &&
                equalsSymbol(board[1][c], symbol) &&
                equalsSymbol(board[2][c], symbol)) {
                return new JButton[]{board[0][c], board[1][c], board[2][c]};
            }
        }

        // Diagonal top-left to bottom-right
        if (equalsSymbol(board[0][0], symbol) &&
            equalsSymbol(board[1][1], symbol) &&
            equalsSymbol(board[2][2], symbol)) {
            return new JButton[]{board[0][0], board[1][1], board[2][2]};
        }

        // Diagonal top-right to bottom-left
        if (equalsSymbol(board[0][2], symbol) &&
            equalsSymbol(board[1][1], symbol) &&
            equalsSymbol(board[2][0], symbol)) {
            return new JButton[]{board[0][2], board[1][1], board[2][0]};
        }

        // No winner
        return null;
    }

    // Check if board is completely filled
    boolean isFull() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (board[r][c].getText().trim().equals(""))
                    return false;
        return true;
    }

    // Helper: safely compare button text to symbol
    private boolean equalsSymbol(JButton btn, String symbol) {
        return btn.getText() != null && btn.getText().trim().equals(symbol);
    }
}

public class TicTacToe {
    JFrame frame = new JFrame("Tic-Tac-Toe");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    Player playerX, playerO, currentPlayer;
    GameBoard gameBoard = new GameBoard();
    boolean gameOver = false;

    Map<String, int[]> stats = new HashMap<>();
    File statsFile = new File("stats.txt");

    public TicTacToe() {
        loadStats();

        // Ask nicknames
        String nameX = JOptionPane.showInputDialog(frame, "Enter Player X nickname:");
        if (nameX == null || nameX.isEmpty()) nameX = "Player X";
        String nameO = JOptionPane.showInputDialog(frame, "Enter Player O nickname:");
        if (nameO == null || nameO.isEmpty()) nameO = "Player O";

        stats.putIfAbsent(nameX, new int[]{0,0});
        stats.putIfAbsent(nameO, new int[]{0,0});

        playerX = new Player(nameX, "X", stats.get(nameX)[0], stats.get(nameX)[1]);
        playerO = new Player(nameO, "O", stats.get(nameO)[0], stats.get(nameO)[1]);

        currentPlayer = playerX;

        setupGUI();
    }

    private void setupGUI() {
        frame.setSize(600, 650);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setBackground(Color.darkGray);
        textLabel.setForeground(Color.white);
        textLabel.setFont(new Font("Arial", Font.BOLD, 50));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setOpaque(true);
        textLabel.setText(currentPlayer.name + "'s turn (" + currentPlayer.symbol + ")");

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(3,3));
        boardPanel.setBackground(Color.darkGray);
        frame.add(boardPanel, BorderLayout.CENTER);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton tile = new JButton();
                gameBoard.board[r][c] = tile;
                boardPanel.add(tile);

                tile.setBackground(Color.white);
                tile.setForeground(Color.darkGray);
                tile.setFont(new Font("Arial", Font.BOLD, 120));
                tile.setFocusable(false);

                tile.addActionListener(e -> {
                    if (gameOver) return;
                    JButton btn = (JButton)e.getSource();
                    if (btn.getText().equals("")) {
                        btn.setText(currentPlayer.symbol);
                        checkGame();
                        if (!gameOver) switchPlayer();
                    } else {
                        JOptionPane.showMessageDialog(frame, "This spot is already taken!");
                    }
                });
            }
        }

        frame.setVisible(true);
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == playerX) ? playerO : playerX;
        textLabel.setText(currentPlayer.name + "'s turn (" + currentPlayer.symbol + ")");
    }

    private void checkGame() {
        JButton[] winningLine = gameBoard.getWinningLine(currentPlayer.symbol);
        if (winningLine != null) {
            gameOver = true;
            textLabel.setText(currentPlayer.name + " wins!");
            currentPlayer.wins++;
            getOpponent(currentPlayer).losses++;
            saveStats();
            highlightWinner(winningLine);
            promptPlayAgain();
        } else if (gameBoard.isFull()) {
            gameOver = true;
            textLabel.setText("Tie game!");
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++) {
                    gameBoard.board[r][c].setForeground(Color.orange);
                    gameBoard.board[r][c].setBackground(Color.gray);
                }
            promptPlayAgain();
        }
    }

    private Player getOpponent(Player player) {
        return (player == playerX) ? playerO : playerX;
    }

    private void highlightWinner(JButton[] winningLine) {
        for (JButton btn : winningLine) {
            btn.setForeground(Color.green);
        }
    }

    private void promptPlayAgain() {
        int choice = JOptionPane.showConfirmDialog(frame, "Play again?", "Restart Game", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // Alternate starting player
            currentPlayer = (currentPlayer == playerX) ? playerO : playerX;
            gameBoard.resetBoard();
            gameOver = false;
            textLabel.setText(currentPlayer.name + "'s turn (" + currentPlayer.symbol + ")");
        } else {
            JOptionPane.showMessageDialog(frame,
                playerX.name + " stats: " + playerX.wins + " Wins / " + playerX.losses + " Losses\n" +
                playerO.name + " stats: " + playerO.wins + " Wins / " + playerO.losses + " Losses",
                "Final Stats",
                JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    private void loadStats() {
        if (!statsFile.exists()) return;
        try (Scanner sc = new Scanner(statsFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    int wins = Integer.parseInt(parts[1].replace("W:", ""));
                    int losses = Integer.parseInt(parts[2].replace("L:", ""));
                    stats.put(name, new int[]{wins, losses});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveStats() {
        stats.put(playerX.name, new int[]{playerX.wins, playerX.losses});
        stats.put(playerO.name, new int[]{playerO.wins, playerO.losses});
        try (PrintWriter pw = new PrintWriter(statsFile)) {
            for (Map.Entry<String,int[]> entry : stats.entrySet()) {
                pw.println(entry.getKey() + ",W:" + entry.getValue()[0] + ",L:" + entry.getValue()[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TicTacToe();
    }
}
