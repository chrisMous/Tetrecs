package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;

    //holds the current piece
    protected PieceBoard currentPiece;
    //holds the next piece
    protected PieceBoard followingPiece;

    protected GameBoard board;

    protected int rowPosition;
    protected int columnPosition;

    //progress bar that displays when the game loop ends
    protected ProgressBar timerBar = new ProgressBar();
    //holds the high score that is displayed
    protected IntegerProperty highScore = new SimpleIntegerProperty(0);
    Timeline timeline;
    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add(SettingsScene.style());
        root.getChildren().add(challengePane);

        //border pane that deals with the position of objects in the scene
        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //set up the gameBoard
        board = new GameBoard(this.game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);
        board.getStyleClass().add("gameBox1");

        //grid that holds the components of the top part of the scene
        var topPart = new GridPane();
        mainPane.setTop(topPart);
        topPart.setPadding(new Insets(10,10,10,10));

        //VBox that holds the components of the right part of the scene
        var rightPart = new VBox();
        BorderPane.setAlignment(rightPart,Pos.CENTER_RIGHT);
        mainPane.setRight(rightPart);
        rightPart.setPadding(new Insets(3,5,2,5));

        //label for the score
        var scoreLabel = new Text("Score");
        scoreLabel.getStyleClass().add("heading");
        topPart.add(scoreLabel,0,0);

        //text that displays the score bound to the games getScore method
        var score = new Text("0");
        score.textProperty().bind(game.getScore().asString());
        score.getStyleClass().add("score");
        topPart.add(score,0,1);

        //label that displays the mode type
        var modeLabel = new Text("Challenge Mode");
        modeLabel.getStyleClass().add("title");
        topPart.add(modeLabel,1,1);
        GridPane.setHgrow(modeLabel,Priority.ALWAYS);
        GridPane.setHalignment(modeLabel, HPos.CENTER);

        //label for the lives
        var livesLabel = new Text("Lives");
        livesLabel.getStyleClass().add("heading");
        topPart.add(livesLabel,2,0);

        //text that holds the lives bound to the game's getLives method
        var lives = new Text("3");
        lives.textProperty().bind(game.getLives().asString());
        lives.getStyleClass().add("lives");
        topPart.add(lives,2,1);

        //label for the multiplier
        var multiplierLabel = new Text("Multiplier");
        multiplierLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(multiplierLabel);

        //text that holds the multiplier bound to the game's getMultiplier method
        var multiplier = new Text();
        multiplier.textProperty().bind(game.getMultiplier().asString().concat("X"));
        multiplier.getStyleClass().add("lives");
        rightPart.getChildren().add(multiplier);

        //label for the level
        var levelLabel = new Text("Level");
        levelLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(levelLabel);

        //text that holds the level bound to the game's getLevel method
        var level = new Text();
        level.textProperty().bind(game.getLevel().asString());
        level.getStyleClass().add("level");
        rightPart.getChildren().add(level);

        //label for the high score to beat
        Text highScoreLabel = new Text("High Score");
        highScoreLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(highScoreLabel);

        //text that holds the high score bound to the value of the highscore variable
        Text highScore = new Text();
        highScore.getStyleClass().add("hiscore");
        rightPart.getChildren().add(highScore);
        highScore.textProperty().bind(this.highScore.asString());

        //label that indicates the incoming block
        var incomingLabel = new Text("     Incoming");
        incomingLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(incomingLabel);

        //displays the current piece in a pieceBoard object
        currentPiece = new PieceBoard(gameWindow.getWidth()/6,gameWindow.getHeight()/6);
        currentPiece.getStyleClass().add("gameBox");
        rightPart.getChildren().add(currentPiece);
        currentPiece.showMiddleCircle();

        //displays the next piece in a pieceBoard object
       followingPiece = new PieceBoard(gameWindow.getWidth()/11,gameWindow.getHeight()/11);
       followingPiece.getStyleClass().add("gameBox");
       rightPart.getChildren().add(followingPiece);
       rightPart.setSpacing(10);

       //sets the progressBar at the bottom and binds it to the width of the border pane
        timerBar.prefWidthProperty().bind(mainPane.widthProperty());
        mainPane.setBottom(timerBar);


        //Handle block on gameboard grid being clicked
        this.board.setOnBlockClick(this::blockClicked);

        //Handle rotation on block when right clicked
        this.board.setOnRightClick(this::rotate);

        //Handle rotation on block when pieceBoard grid is clicked
        this.currentPiece.setOnBlockClick(this::rotate);


    }

    /**
     * rotates the block
     * @param gameBlock to be rotated
     */
    private void rotate(GameBlock gameBlock) {
        rotate();
    }

    /**
     * displays the next and current pieces on the pieceBoard
     * @param nextPiece is the next piece to be displayed
     */
    protected void nextPiece(GamePiece nextPiece) {
       this.currentPiece.displayPiece(nextPiece);
       this.followingPiece.displayPiece(game.getFollowingPiece());
    }


    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        if(this.game.blockClicked(gameBlock))
            game.resetGameLoop();
    }

    /**
     * rotates and displays the rotated piece on the pieceBoard
     */
    public void rotate(){
        game.rotatePiece();
        currentPiece.displayPiece(game.getCurrentPiece());
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * rotates and displays the rotated piece on the pieceBoard
     * @param rotations stores the type of rotation to be performed
     */
    public void rotate(int rotations){
        game.rotatePiece(rotations);
        currentPiece.displayPiece(game.getCurrentPiece());
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * swaps the current piece with the next one and displays
     * this change in the pieceBoards
     */
    public void swap(){
        game.swapCurrentPiece();
        currentPiece.displayPiece(game.getCurrentPiece());
        followingPiece.displayPiece(game.getFollowingPiece());
        Multimedia.playAudio("rotate.wav");
    }

        /**
         * Setup the game object and model
         */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        this.game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise(){
        logger.info("Initialising Challenge");

        //listens to when the next piece is fetched and calls the respective in class method
        this.game.setNextPieceListener(this::nextPiece);

        //listens to when a key is pressed and calls the respective in class method
        this.scene.setOnKeyPressed(this::pressedKey);

        //listens to when a line is cleared and calls the respective in class method
        this.game.setLineClearedListener(this::clearedLine);

        //listens to when a game loop starts and calls the respective in class method
        this.game.setOnGameLoop(this::gameLoop);

        //holds the scores and loads them from the scores scene
        ArrayList<Pair<String, Integer>> scores = ScoresScene.loadScores();
        //displays the high score wich is the first item of the scores array
        highScore.set((scores.get(0)).getValue());

        //starts the game
        game.start();

        //after the game is over the game stops and the scores scene is displayed
        this.game.setOnGameOver(() -> {
           game.stop();
           timeline.stop();
           gameWindow.startScores(this.game);
        });

    }

    /**
     * handles the events that occur when a key is pressed
     * this method enables keyboard support
     * @param key is the key that is pressed
     */
    public void pressedKey(KeyEvent key){
        //when escape is pressed the game is stopped and the player is brought
        //back to the menu
        if(key.getCode().equals(KeyCode.ESCAPE)){
            game.stop();
            timeline.stop();
            gameWindow.startMenu();
        }
        //when space or r is pressed the pieces swap
        else if(key.getCode().equals(KeyCode.SPACE) || key.getCode().equals(KeyCode.R)){
            swap();
        }
        //when enter or x is pressed the block is placed
        else if(key.getCode().equals(KeyCode.ENTER) || key.getCode().equals(KeyCode.X)){
            blockClicked(board.getBlock(rowPosition,columnPosition));
        }
        //when right arrow or d is pressed the block is moved one block to the right
        else if(key.getCode().equals(KeyCode.RIGHT) || key.getCode().equals(KeyCode.D)){
            if(rowPosition < game.getCols() - 1 ){
                rowPosition++;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        //when left arrow or a is pressed the block is moved one block to the left
        else if(key.getCode().equals(KeyCode.LEFT) || key.getCode().equals(KeyCode.A)){
            if(rowPosition > 0){
                rowPosition--;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        //when up arrow or w is pressed the block is moved one block up
        else if(key.getCode().equals(KeyCode.UP) || key.getCode().equals(KeyCode.W)){
            if(columnPosition > 0){
                columnPosition--;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        //when downwards arrow or s is pressed the block is moved one block down
        else if(key.getCode().equals(KeyCode.DOWN) || key.getCode().equals(KeyCode.S)){
            if(columnPosition < game.getRows() - 1 ){
                columnPosition++;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        //when the open bracket or q is pressed the block rotates to the left
        else if(key.getCode().equals(KeyCode.OPEN_BRACKET) || key.getCode().equals(KeyCode.Q) || key.getCode().equals(KeyCode.Z) ){
            rotate(3);
        }
        //when the close bracket or e is pressed the block rotates to the right
        else if(key.getCode().equals(KeyCode.CLOSE_BRACKET) || key.getCode().equals(KeyCode.E) || key.getCode().equals(KeyCode.C) ){
            rotate(1);
        }
    }

    /**
     * handles the clearing and fade out animation on the board
     * @param blockCoordinates are the blocks to be faded
     */
    public void clearedLine(HashSet<GameBlockCoordinate> blockCoordinates){
        board.fadeOut(blockCoordinates);
    }

    /**
     * handles displaying the progressBar's movement according to the duration of the time delay
     * @param nextLoop holds the time until the next loop
     */
    protected void gameLoop(int nextLoop) {
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timerBar.progressProperty(), 1)),
                new KeyFrame(Duration.millis(game.getTimeDelay()), e-> {
                }, new KeyValue(timerBar.progressProperty(), 0))
        );
        timeline.play();
  }

}
