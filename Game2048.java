import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Constructs game 2048.
 *
 * @author Dian Yang
 * @version 1.0
 */
public class Game2048 extends JPanel {
    private static final int SIDE = 4;
    private static final int TARGET = 2048;
    private int score;
    enum State {
        start, won, running, over
    }
    private State gameState = State.start;
    private final Color gridColor = new Color(0x987A5E35, true);
    private final Color emptyColor = new Color(0x98FFDFCD, true);
    private final Color tileColor = new Color(0xCBFFDFCD);
    private final Color textColor = new Color(0xCC4C1D);
    private final Random random = new Random();
    private boolean checkAvailableMove;
    private Tile[][] tiles;

    /**
     * Constructor. Sets features of the window.
     */
    public Game2048() {
        setPreferredSize(new Dimension(900, 600));
        setBackground(Color.WHITE);
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startGame();
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        moveDown();
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRight();
                        break;
                }
                repaint();
            }
        });
    }

    /**
     * Method for starting the game. Sets variables to initial values, adds two tiles to grid.
     */
    void startGame() {
        if (gameState == State.running)
            return;
        score = 0;
        tiles = new Tile[SIDE][SIDE];
        gameState = State.running;
        addRandomTile();
        addRandomTile();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        draw(g2);
    }

    /**
     * Draws different objects on the window based on state of game. It is called every time the window is repainted.
     *
     * @param g2 the Graphics2D object to be modified.
     */
    void draw(Graphics2D g2) {
        // Creates a square for the game.
        g2.setColor(gridColor);
        g2.fillRoundRect(150, 50, 501, 501, 15, 15);

        if (gameState == State.start) {
            g2.setColor(textColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 100));
            g2.drawString("2048", 270, 200);
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2.drawString("click to start", 330, 400);
            g2.drawString("use arrow keys to move", 280, 450);
        } else if (gameState == State.won) {
            g2.setColor(textColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 30));
            g2.drawString("Target achieved!", 350, 300);
        } else if (gameState == State.over) {
            g2.setColor(textColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 50));
            g2.drawString("Game over", 280, 300);
            g2.setFont(new Font("SansSerif", Font.BOLD, 25));
            g2.drawString("Score: " + score, 320, 380);
            g2.drawString("Click to start a new game", 260, 420);
        } else {
            // updates empty grids and tiles
            for (int i = 0; i < SIDE; i++) {
                for (int j = 0; j < SIDE; j++) {
                    if (tiles[i][j] == null) {
                        g2.setColor(emptyColor);
                        g2.fillRoundRect(170 + j * 120, 70 + i * 120, 100, 100, 7, 7);
                    } else {
                        drawTile(g2, i, j);
                    }
                }
            }
            // updates score of the game
            g2.setFont(new Font("SansSerif", Font.BOLD, 30));
            g2.setColor(textColor);
            g2.drawString("SCORE: " + score, 680, 130);
        }
    }

    /**
     * Draws a tile in the table.
     *
     * @param g2 the Graphics2D object to be modified.
     * @param x row of the tile
     * @param y column of the tile
     */
    void drawTile(Graphics2D g2, int x, int y) {
        g2.setColor(tileColor);
        g2.fillRoundRect(170 + y * 120, 70 + x * 120, 100, 100, 7, 7);
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2.setColor(textColor);
        int val = tiles[x][y].getValue();
        g2.drawString(String.valueOf(val), (int) (220 - 8 * Math.log(val) + y * 120), 130 + x * 120);
    }

    /**
     * Moves the tiles successively in the table based on start point and direction.
     *
     * @param startPoint specifies the grid to be moved first, e.g. the top left one when moving up or left,
     *                   the bottom right one when moving down or right.
     * @param xOffset the increment in rows. -1 when moving up and 1 when moving down.
     * @param yOffset the increment in columns. -1 when moving left and 1 when moving right.
     * @return if any tile is moved or able to be moved.
     */
    boolean move(int startPoint, int xOffset, int yOffset) {
        boolean isMoved = false;
        for (int i = 0; i < SIDE * SIDE; i++) {
            int x = Math.abs(startPoint - i) / SIDE;
            int y = Math.abs(startPoint - i) % SIDE;
            if (tiles[x][y] == null)
                continue;
            int nextX = x + xOffset;
            int nextY = y + yOffset;

            while (nextX >= 0 && nextX < SIDE && nextY >= 0 && nextY < SIDE) {
                Tile current = tiles[x][y];
                Tile next = tiles[nextX][nextY];

                if (tiles[nextX][nextY] == null) {
                    // If it is checking available moves and there is an empty grid, return true.
                    if (checkAvailableMove)
                        return true;
                    tiles[nextX][nextY] = current;
                    tiles[x][y] = null;
                    x = nextX;
                    y = nextY;
                    nextX += xOffset;
                    nextY += yOffset;
                    isMoved = true;
                } else if (next.canMergeWith(current)) {
                    // f it is checking available moves and there is a tile that can merge with others, return true.
                    if (checkAvailableMove)
                        return true;
                    next.mergeWith(current);
                    score += next.getValue();
                    tiles[x][y] = null;
                    isMoved = true;
                    break;
                } else {
                    break;
                }
            }
        }

        // If any tile is moved, set all tiles to be able to merge, add a tile to the table, and change state of game
        // if needed.
        if (isMoved) {
            clearMerge();
            addRandomTile();
            if (!moveAvailable()) {
                gameState = State.over;
            }
            if (score == TARGET) {
                gameState = State.won;
            }
        }
        return isMoved;
    }

    /**
     * Each of the following four methods moves all tiles in a certain direction.
     *
     * @return if there is any tile able to move in that direction.
     */
    boolean moveUp() {
        return move(0, -1, 0);
    }

    boolean moveDown() {
        return move(SIDE * SIDE - 1, 1, 0);
    }

    boolean moveLeft() {
        return move(0, 0, -1);
    }

    boolean moveRight() {
        return move(SIDE * SIDE - 1, 0, 1);
    }

    /**
     * Checks if there is any move available.
     *
     * @return if there is any move available.
     */
    boolean moveAvailable() {
        checkAvailableMove = true;
        boolean canMove = moveUp() || moveDown() || moveLeft() || moveRight();
        checkAvailableMove = false;
        return canMove;
    }

    /**
     * Sets all tiles to be able to merge with other tiles.
     */
    void clearMerge() {
        for (int i = 0; i < SIDE; i++) {
            for (int j = 0; j < SIDE; j++) {
                if (tiles[i][j] == null)
                    continue;
                tiles[i][j].clearMerge();
            }
        }
    }

    /**
     * Adds a tile with the value 2 or 4 to the table.
     */
    void addRandomTile() {
        int row, col;
        do {
            row = random.nextInt(SIDE);
            col = random.nextInt(SIDE);
        } while (tiles[row][col] != null);
        int val = random.nextInt(2) == 0 ? 2 : 4;
        tiles[row][col] = new Tile(val);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("2048");
            f.setResizable(true);
            f.add(new Game2048(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}

/**
 * Tile class used for creating tiles in the table.
 */
class Tile {
    private boolean isMerged;
    private int val;

    Tile(int val) {
        this.val = val;
    }

    int getValue() {
        return val;
    }

    void clearMerge() {
        isMerged = false;
    }

    /**
     * Checks if this tile can merge with another tile.
     * @param t the tile to be merged with
     * @return if this tile can merge with another tile
     */
    boolean canMergeWith(Tile t) {
        return !isMerged && !t.isMerged && val == t.getValue();
    }

    /**
     * Merges this tile with another tile.
     * @param t the tile to be merged with
     */
    void mergeWith(Tile t) {
        if (!canMergeWith(t))
            return;
        val *= 2;
        isMerged = true;
    }
}
