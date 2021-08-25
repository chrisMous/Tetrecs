package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.*;
import java.util.concurrent.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    private Random random;

    //Game Piece objects for the current and next piece are initialised
    protected GamePiece currentPiece;
    protected GamePiece followingPiece;

    //Integer properties for the score, level, lives and multiplier set
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(0);
    private final IntegerProperty lives = new SimpleIntegerProperty(3 );
    private final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /*
       listeners that handle the next piece, the lines cleared, the game loop
        and the termination of the game
     */
    protected NextPieceListener nextPieceListener = null;
    protected LineClearedListener lineClearedListener = null;
    protected GameLoopListener gameLooplistener = null;
    protected GameOverListener gameOverlistener = null;

    protected int timeDelay;

    //used to create a scheduled future for the game loop
    private final ScheduledExecutorService timer =  Executors.newSingleThreadScheduledExecutor();
    protected ScheduledFuture<?> scheduledFuture;

    //stores the scores
    protected ArrayList<Pair<String,Integer>> scores = new ArrayList();
    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startGameLoop();
    }

    /**
     * Stop the game
     */
    public void stop(){
        logger.info("Terminating game");
        timer.shutdownNow();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        Multimedia.playMusic("game.wav");
        followingPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the x and y positions of this block - 1 so it's placed on its centre
        int x = gameBlock.getX()-1;
        int y = gameBlock.getY()-1;

         /*
            this boolean value will determine if it's possible to place the current
            piece from its centre
         */
        boolean centeredPiece = grid.playPiece(currentPiece,x,y);

        //if the piece cannot be placed from its centre, then it's not placed
        if(!centeredPiece){
            logger.info("Cannot place piece!");
            Multimedia.playAudio("fail.wav");
           return false;
        }
        //if the piece can be placed it's placed and the nextPiece and afterPiece methods are called
    else {
            nextPiece();
            afterPiece();
            Multimedia.playAudio("place.wav");
            return true;
        }
    }

    /**
     * spawns a random piece
     * @return a random piece
     */
    public GamePiece spawnPiece(){
        random = new Random();
        //selects a random value for the piece from 0-14 included
        int randomPiece = random.nextInt(15);
        //creates and returns th piece
        return GamePiece.createPiece(randomPiece);
    }

    /**
     * fetches the next piece of the game
     * @return the current piece to be displayed
     */
    public GamePiece nextPiece(){

        //the current piece is set to the next piece
        this.currentPiece = this.followingPiece;

        //the next piece is created
        followingPiece = spawnPiece();

        if (this.nextPieceListener != null) {
            this.nextPieceListener.nextPiece(this.currentPiece);
        }
        logger.info("The next piece is: {}", followingPiece);

        return this.currentPiece;
    }


    /**
     * clears the line of the row or column that has full blocks
     */
    public void afterPiece(){
        int linesCleared = 0;
        int blocksCleared = 0;

        //cleared blocks are stored in a hash set
        HashSet<GameBlockCoordinate> clearedBlocks = new HashSet<>();

        //removes rows that have full blocks in them
        for (int y = 0; y < getRows(); y++) {
            //stores the number of rows
            int rowNum = getRows();

            //loops through the column values from the grid that are not 0
            for (int i = 0; i < getCols() && this.grid.get(i, y) != 0; i++){
                rowNum--;
        }
            /* when all the rows are checked, the rows that are full are removed,
               because the values of their blocks are set 0
            */
            if (rowNum == 0) {
                for (int i = 0; i < getCols(); i++) {
                    blocksCleared++;
                    //the value at the point of the cleared block is set to 0
                    grid.set(i,y,0);
                    //cleared blocks are added to the hash set
                    clearedBlocks.add(new GameBlockCoordinate(i,y));
                }
                linesCleared++;
                Multimedia.playAudio("clear.wav");
                logger.info("Removed Row!");
            }
        }

        //removes columns that have full blocks in them
        for (int x = 0; x < getCols() ; x++) {
            int rowNum = getRows();

            //loops through the row values from the grid that are not 0
            for (int i = 0; i < getRows() && this.grid.get(x, i) != 0; i++){
                rowNum--;
        }
            /* when all the rows are checked, the columns that are full are removed,
               because the values of their blocks are set to 0
            */
            if (rowNum == 0) {
                for (int i = 0; i < getRows() ; i++) {
                    blocksCleared++;
                    //the value at the point of the cleared block is set to 0
                    grid.set(x,i,0);
                    //cleared blocks are added to the hash set
                    clearedBlocks.add(new GameBlockCoordinate(x,i));
                }
                linesCleared++;
                Multimedia.playAudio("clear.wav");
                logger.info("Removed Column!");
            }
        }
        //if no lines are cleared the multiplier is reset back to 1
        if(linesCleared == 0){
            multiplier.set(1);
        }
        //if more than 0 lines are cleared the multiplier gets one point
        if (linesCleared > 0)
            multiplier.set(multiplier.add(1).get());

        //lines cleared and blocks cleared are passed to the score method
        score(linesCleared,blocksCleared);

        if(lineClearedListener != null){
            lineClearedListener.lineCleared(clearedBlocks);
        }

        //the level is set so that every 1000 points of score the level gets one
        level.set(Math.floorDiv(getScore().get(), 1000));


    }

    /**
     * rotates the current piece
     */
    public void rotatePiece(){
        currentPiece.rotate();
    }

    /**
     * rotates the current piece but this time the rotation can be chosen
     * @param rotations stores the value of the rotation to be given
     */
    public void rotatePiece(int rotations){
        currentPiece.rotate(rotations);
    }

    /**
     * swaps the current piece with the next one and vice versa
     */
    public void swapCurrentPiece(){
        GamePiece piece = getCurrentPiece();
        currentPiece = getFollowingPiece();
        followingPiece = piece;
        logger.info("Swapping Pieces");
    }

    /**
     * calculates the score of the game with the calculation,
     * lines cleared * blocks cleared * multplier *10
     * @param linesCleared stores the lines cleared
     * @param blocksCleared stores the blocks cleared
     */
    public void score(int linesCleared,int blocksCleared){
        //if no lines are cleared the score stays the same
        if(linesCleared == 0)
            return;
        else{
            this.score.set(score.add(linesCleared * blocksCleared * 10 * multiplier.get()).get());
            logger.info("Score updated: {}", getScore().get());
        }
    }

    /**
     * calculates the time delay for the game loop
     * @return the maximum between 2500 and the calculated number
     */
    public int getTimeDelay(){
        int max = 12000 - (500 * level.get());
        int max2 = 2500;
        return Math.max(max, max2);
    }

    /**
     * creates the game loop
     */
    public void gameLoop(){
        //if the lives reach 0 the game is terminated
        if(lives.get() == 0){
            if(gameOverlistener != null) {
                logger.info("GAME OVER");
                Platform.runLater(() -> gameOverlistener.endGame());
                return;
            }
        }
        //the lives are decreased by 1
        lives.set(getLives().subtract(1).get());
        Multimedia.playAudio("lifelose.wav");
        //the piece is changed with the next one
        nextPiece();
        //the multiplier is set back to 1
        multiplier.set(1);

       this.timeDelay = getTimeDelay();
        logger.info("Starting Game Loop");

       if(gameLooplistener != null)
           this.gameLooplistener.loop(timeDelay);

       /*
        * a scheduled future is scheduled by the ScheduledExecutorService,
        * the game loop is executed with the time delay in milliseconds
        */
       scheduledFuture = timer.schedule(this::gameLoop,getTimeDelay(),TimeUnit.MILLISECONDS);
    }

    /**
     * starts the game loop
     */
    public void startGameLoop(){
       scheduledFuture = timer.schedule(this::gameLoop,getTimeDelay(), TimeUnit.MILLISECONDS);
        if(gameLooplistener != null)
            this.gameLooplistener.loop(getTimeDelay());

    }

    /**
     * restarts the game loop without changing any values
     */
    public void resetGameLoop(){
        scheduledFuture.cancel(false);
        startGameLoop();
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the next piece of the game
     * @return teh next piece
     */
    public GamePiece getFollowingPiece(){
        return followingPiece;
    }

    /**
     * Get the current piece of the game
     * @return the current piece
     */
    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * Getter methods for the score,level,lives,multiplier and scores array
     * @return
     */
    public IntegerProperty getScore(){
        return score;
    }
    public IntegerProperty getLevel(){
        return level;
    }
    public IntegerProperty getLives(){
        return lives;
    }
    public IntegerProperty getMultiplier(){
        return multiplier;
    }
    public ArrayList<Pair<String, Integer>> getScores() {
        return scores;
    }

    /**
     * Setter methods for the listeners used in this class
     * @param listener the respective listener
     */
    public void setNextPieceListener(NextPieceListener listener){
        this.nextPieceListener = listener;
    }
    public void setLineClearedListener(LineClearedListener listener){
        this.lineClearedListener = listener;
    }
    public void setOnGameLoop(GameLoopListener listener){
        this.gameLooplistener = listener;
    }
    public void setOnGameOver(GameOverListener listener){
        this.gameOverlistener = listener;
    }
}
