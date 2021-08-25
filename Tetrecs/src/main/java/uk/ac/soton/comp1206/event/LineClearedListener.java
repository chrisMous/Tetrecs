package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * Handles the clearing of a line when it gets full
 * it passes the coordinates of the blocks that need to be cleared
 */
public interface LineClearedListener {
    /**
     * stores the game blocks that are cleared and enables the fading effect to work
     * @param blockCoordinates are the x and y coordinates of a block as a set
     */
    void lineCleared(HashSet<GameBlockCoordinate> blockCoordinates);
}
