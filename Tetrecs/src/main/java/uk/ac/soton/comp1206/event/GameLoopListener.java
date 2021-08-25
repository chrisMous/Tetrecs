package uk.ac.soton.comp1206.event;

/**
 * The GameLoopListener is used to handle the event of a game loop
 * and passes the time of the game loop
 */
public interface GameLoopListener {
    /**
     * handle a game loop
     * @param time the time before the game loop ends
     */
    void loop(int time);
}
