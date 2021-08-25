package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Handles and stores the next piece to be fetched and become the current piece after the
 * current piece is placed
 */
public interface NextPieceListener {
    /**
     * handle the next piece to be placed
     * @param gamePiece the next piece
     */
    void nextPiece(GamePiece gamePiece);
}
