package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     *
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     *
     * @param x     column
     * @param y     row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    public void clearGrid() {
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.cols; x++)
                this.grid[x][y].set(0);
        }
    }
        /**
         * Get the value represented at the given x and y index within the grid
         *
         * @param x column
         * @param y row
         * @return the value
         */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * checks if  apiece can be placed in a certain position
     * @param gamePiece the piece that is going to be placed
     * @param xValue x coordinate of the piece
     * @param yValue y coordinate of the piece
     * @return true or false based on if the piece can be placed
     */
    public boolean canPlayPiece(GamePiece gamePiece, int xValue, int yValue) {

        int[][] gridBlocks = gamePiece.getBlocks();

        //nested for loop to iterate through the coordinates of the grid block
        for (int x = 0; x < gridBlocks.length; x++) {
            for (int y = 0; y < gridBlocks[x].length; y++) {
                int blockValue = gridBlocks[x][y];

                if (blockValue != 0) {
                    //gets the grid value of the positions of x,y plus the x,y values given
                    int gridValue = get(x + xValue, y + yValue);
                    //if the gridValue is not equal to 0 there's no space available to place a piece
                    if (gridValue != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * places the piece for the specified x and y values
     * @param gamePiece the game piece to be placed
     * @param xValue x coordinate of the piece
     * @param yValue y coordinate of the piece
     * @return true if the piece can be placed false if not
     */
    public boolean playPiece(GamePiece gamePiece, int xValue, int yValue) {
        int[][] gridBlocks = gamePiece.getBlocks();
        //if the piece cannot be played then the method returns false
        if (!canPlayPiece(gamePiece, xValue, yValue))
            return false;
        else {
            for (int x = 0; x < gridBlocks.length; x++) {
                for (int y = 0; y < gridBlocks[x].length; y++) {
                    int blockValue = gridBlocks[x][y];

                    if (blockValue != 0){
                        set(x + xValue , y + yValue , blockValue);
                    }
                }
            }
        }

        return true;
    }

}