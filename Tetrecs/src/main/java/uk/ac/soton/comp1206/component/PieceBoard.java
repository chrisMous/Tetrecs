package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The PieceBoard class extends the GameBoard class and handles the
 * appearance of the current and next piece at the right side of the
 * single player and multiplayer challenges
 */
public class PieceBoard extends GameBoard {

    public PieceBoard(double width, double height) {
        //the piece board always has 3x3 grid dimensions
        super(3, 3, width, height);

    }

    /**
     * handles displaying a piece inside the piece board
     * so the user knows how his piece looks like
     * @param gamePiece is the piece to be displayed
     */
    public void displayPiece(GamePiece gamePiece) {
        this.grid.clearGrid();
        grid.playPiece(gamePiece, 0, 0);
    }

    /**
     * handles showing the circle at the centre of the game piece grid
     * so the player knows where the piece is placed from
     */
    public void showMiddleCircle() {
        //the X and Y midpoints of the piece board are determined
        double midX = Math.ceil(rows / 2);
        double midY = Math.ceil(cols / 2);

        //the midpoint of those blocks sets the setCentre method true
        //as it's the centre of the block
        this.blocks[(int) midX][(int) midY].setCentre(true);
    }
}
