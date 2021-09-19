package com.example.game2048_javafx;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.Random;

/**
 * Constructs game 2048 (five in a row). JavaFX version.
 * See gameplay at https://en.wikipedia.org/wiki/2048_(video_game)
 *
 * @author Dian Yang
 * @version 1.0
 */
public class Game2048 extends Application {
    private static final int SIDE = 4;
    private static final int TARGET = 2048;
    private int score;
    enum State {
        start, won, running, over
    }
    private State gameState = State.start;
    private final Color emptyColor = Color.BURLYWOOD;
    private final Color tileColor = Color.BISQUE;
    private final Color textColor = Color.DARKORANGE;
    private boolean checkAvailableMove;
    private final Random random = new Random();
    private Tile[][] tiles;
    private Label scoreLabel;
    private Rectangle grid;
    private Pane pane;

    /**
     * Constructor. Instantiates class variables, creates a grid for the game.
     * Starts the game when clicking mouse.
     */
    public Game2048() {
        pane = new Pane();
        grid = new Rectangle(150, 50, 501, 501);
        grid.setArcHeight(15);
        grid.setArcWidth(15);
        grid.setFill(Color.SADDLEBROWN);
        pane.getChildren().add(grid);
        initialize();
        grid.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                startGame();
                draw();
            }
        });
    }

    /**
     * Set initial display of the game.
     */
    void initialize() {
        Label title = new Label("2048");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 120));
        title.setTranslateX(280);
        title.setTranslateY(150);
        Label instr1 = new Label("click to start");
        instr1.setFont(Font.font("SansSerif", FontWeight.BOLD, 25));
        instr1.setTranslateX(330);
        instr1.setTranslateY(400);
        Label instr2 = new Label("use arrow keys to move");
        instr2.setFont(Font.font("SansSerif", FontWeight.BOLD, 25));
        instr2.setTranslateX(280);
        instr2.setTranslateY(450);
        pane.getChildren().addAll(title, instr1, instr2);
    }

    /**
     * Method for starting the game. Sets variables to initial values, adds two tiles to grid.
     */
    void startGame() {
        if (gameState == State.running)
            return;
        grid = new Rectangle(150, 50, 501, 501);
        grid.setArcHeight(15);
        grid.setArcWidth(15);
        grid.setFill(Color.SADDLEBROWN);
        pane.getChildren().add(grid);
        score = 0;
        tiles = new Tile[SIDE][SIDE];
        gameState = State.running;
        addRandomTile();
        addRandomTile();
    }

    /**
     * Draws tiles and empty grids as well as other nodes.
     * Being called every time an arrow key is pressed.
     */
    void draw() {
        // updates empty grids and tiles
        for (int i = 0; i < SIDE; i++) {
            for (int j = 0; j < SIDE; j++) {
                if (tiles[i][j] == null) {
                    Rectangle emptyTile = new Rectangle(170 + j * 120, 70 + i * 120, 100, 100);
                    emptyTile.setArcWidth(15);
                    emptyTile.setArcHeight(15);
                    emptyTile.setFill(emptyColor);
                    pane.getChildren().add(emptyTile);
                } else {
                    drawTile(i, j);
                }
            }
        }
        // updates score of the game
        pane.getChildren().remove(scoreLabel);
        scoreLabel = new Label("SCORE: " + score);
        scoreLabel.setTranslateX(680);
        scoreLabel.setTranslateY(130);
        scoreLabel.setTextFill(textColor);
        scoreLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 30));
        pane.getChildren().add(scoreLabel);
        // displays game result
        if (gameState != State.running) {
            String res = gameState == State.won ? "Target Achieved!" : "Game Over!";
            Label resLabel = new Label(res);
            resLabel.setTranslateX(280);
            resLabel.setTranslateY(150);
            resLabel.setTextFill(Color.BLACK);
            resLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 80));
            pane.getChildren().add(resLabel);
        }
    }

    /**
     * Draws a tile in the table.
     *
     * @param x row of the tile
     * @param y column of the tile
     */
    void drawTile(int x, int y) {
        Rectangle tile = new Rectangle(170 + y * 120, 70 + x * 120, 100, 100);
        tile.setArcWidth(15);
        tile.setArcHeight(15);
        tile.setFill(tileColor);
        int val = tiles[x][y].getValue();
        Label num = new Label(String.valueOf(val));
        num.setTranslateX(220 - 8 * Math.log(val) + y * 120);
        num.setTranslateY(100 + x * 120);
        num.setTextFill(textColor);
        num.setFont(Font.font("SansSerif", FontWeight.BOLD, 50));
        pane.getChildren().addAll(tile, num);
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

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(pane, 900, 600);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case UP:
                        moveUp();
                        break;
                    case DOWN:
                        moveDown();
                        break;
                    case LEFT:
                        moveLeft();
                        break;
                    case RIGHT:
                        moveRight();
                        break;
                }
                draw();
            }
        });
        stage.setScene(scene);
        stage.setTitle("2048");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

/**
 * com.example.game2048_javafx.Tile class used for creating tiles in the table.
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
