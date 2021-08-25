package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
/**
 * The Multiplayer challenge scene. Holds the UI for the multiplayer game mode in the MultiplayerGame.
 */
public class MultiplayerScene extends ChallengeScene{
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    private Communicator communicator;

    //hold the multiplayer scores to be displayed
    private ObservableList<Pair<String, Integer>> multiplayerScoreList;
    private ArrayList<Pair<String, Integer>> multiplayerScoresArray = new ArrayList<>();
    //holds the username of the player
    private StringProperty username = new SimpleStringProperty();
    //displays the scores of the players inside the game
    private Leaderboard leaderboard;
    //holds messages sent by players
    private VBox chat;
    //textfield that players type the messages
    private TextField text;
    //array list that holds the messages thta are displayed in chat
    private ArrayList<TextFlow> textFlows = new ArrayList<>();

    Text chatLabel;

    static boolean multiplayerGame;

    /**
     * Create a new Multiplayer challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Multiplayer Scene");
        communicator = gameWindow.getCommunicator();
    }
    /**
     * Build the Multiplayer window
     * its appearance is very similar to the single player challenge
     * but with a few adjustments (adjustments are commented)
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("multiplayer-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(this.game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);
        board.getStyleClass().add("gameBox1");

        var topPart = new GridPane();
        mainPane.setTop(topPart);
        topPart.setPadding(new Insets(10,10,10,10));

        var rightPart = new VBox();
        BorderPane.setAlignment(rightPart, Pos.CENTER_RIGHT);
        mainPane.setRight(rightPart);
        rightPart.setPadding(new Insets(5,5,5,5));

        var scoreLabel = new Text("Score");
        scoreLabel.textProperty().bind(this.username);
        scoreLabel.getStyleClass().add("heading");
        topPart.add(scoreLabel,0,0);

        var score = new Text("0");
        score.textProperty().bind(game.getScore().asString());
        score.getStyleClass().add("score");
        topPart.add(score,0,1);

        var modeLabel = new Text("Multiplayer Game");
        modeLabel.getStyleClass().add("title");
        topPart.add(modeLabel,1,1);
        GridPane.setHgrow(modeLabel, Priority.ALWAYS);
        GridPane.setHalignment(modeLabel, HPos.CENTER);

        var livesLabel = new Text("Lives");
        livesLabel.getStyleClass().add("heading");
        topPart.add(livesLabel,2,0);

        var lives = new Text("3");
        lives.textProperty().bind(game.getLives().asString());
        lives.getStyleClass().add("lives");
        topPart.add(lives,2,1);


        var levelLabel = new Text("Level");
        levelLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(levelLabel);

        var level = new Text();
        level.textProperty().bind(game.getLevel().asString());
        level.getStyleClass().add("level");
        rightPart.getChildren().add(level);

        //leaderboard label that indicates the users in the game
        var leaderboardLabel = new Text("Versus");
        leaderboardLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(leaderboardLabel);

        //leaderboard object to display the players in the game
        leaderboard = new Leaderboard();
        leaderboard.getStyleClass().add("leaderboard");
        rightPart.getChildren().add(leaderboard);

        //displays and updates the scores in real time
        multiplayerScoreList = FXCollections.observableArrayList(multiplayerScoresArray);
        SimpleListProperty<Pair<String, Integer>> multiplayerLeaderboard = new SimpleListProperty(multiplayerScoreList);
        leaderboard.scoreProperty().bind(multiplayerLeaderboard);
        leaderboard.getUsernameProperty().bind(username);

        var incomingLabel = new Text("     Incoming");
        incomingLabel.getStyleClass().add("heading");
        rightPart.getChildren().add(incomingLabel);

        currentPiece = new PieceBoard(gameWindow.getWidth()/6,gameWindow.getHeight()/6);
        currentPiece.getStyleClass().add("gameBox");
        rightPart.getChildren().add(currentPiece);
        currentPiece.showMiddleCircle();

        followingPiece = new PieceBoard(gameWindow.getWidth()/11,gameWindow.getHeight()/11);
        followingPiece.getStyleClass().add("gameBox");
        rightPart.getChildren().add(followingPiece);
        rightPart.setSpacing(10);

        var bottomPart = new VBox();
        mainPane.setBottom(bottomPart);

        text = new TextField();
        text.setVisible(false);
        text.setPromptText("Enter message - press TAB to send");
        bottomPart.getChildren().add(text);

        timerBar = new ProgressBar(1.0);
        timerBar.prefWidthProperty().bind(mainPane.widthProperty());
        bottomPart.getChildren().add(timerBar);

        //displays the messages on the left part of the screen
        chat = new VBox();
        mainPane.setLeft(chat);
        chat.setPrefWidth(200);
        chat.setMaxWidth(200);
        chatLabel = new Text("Chat");
        chatLabel.getStyleClass().add("heading");
        chat.getChildren().add(chatLabel);

        this.board.setOnBlockClick(this::blockClicked);
        this.board.setOnRightClick(this::rotate);
        this.currentPiece.setOnBlockClick(this::rotate);

        //when the textfield is open and the suer presses TAB a message is sent to be
        //displayed in the chat
        text.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.TAB)){
                communicator.send("MSG " + text.getText());
                text.clear();
                text.setVisible(false);
             }
        });

    }

    /**
     * Setup the game object and model
     */
    public void setupGame(){
        logger.info("Starting a new MultiplayerGame");

        //Start new Multiplayer game
        this.game = new MultiplayerGame(5, 5,communicator);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise(){
        logger.info("Initialising Multiplayer");
        this.game.setNextPieceListener(this::nextPiece);
        this.scene.setOnKeyPressed(this::pressedKey);
        this.game.setLineClearedListener(this::clearedLine);
        this.game.setOnGameLoop(this::gameLoop);

        ArrayList<Pair<String, Integer>> scores = ScoresScene.loadScores();
        highScore.set((scores.get(0)).getValue());

        game.start();

        this.game.setOnGameOver(() -> {
            //sends a "DIE" message to indicate that a player lost
            communicator.send("DIE");
            game.stop();
            gameWindow.startScores(this.game);
        });

        //the score is updated in real time by a listener that tracks its changes and displays them
    this.game.getScore().addListener((observable, score, updatedScore) -> communicator.send("SCORE " + updatedScore));

        //the lives left are updated in real time by a listener that tracks their changes and displays them
    this.game.getLives().addListener((observable, lives, updatedLives) -> communicator.send("LIVES " + updatedLives));

    communicator.send("NICK");

     //handles the messages received by the communicator from the server
        communicator.addListener(message -> {
            Platform.runLater(() -> this.handleMessages(message));
        });

        setMultiplayerGame();
    }


    private void rotate(GameBlock gameBlock) {
        rotate();
    }

    /**
     * handles the display of multiplayer scores and the removal of a dead player
     * @param message holds the contents of the message form the server
     */
    public void loadMultiplayerScores(String message) {
        multiplayerScoresArray.clear();

        //messages are split into parts
        for (String score : message.split("\n")) {
            String[] parts = score.split(":");
            if (parts.length < 2) continue;
            String name = parts[0];
            String points = parts[1];
            String lives = parts[2];

            if (lives.equals("DEAD")){
                leaderboard.removePlayer(name);
            }
            //scores are added to the array list to be displayed
            multiplayerScoresArray.add(new Pair(name, Integer.valueOf(points)));
            logger.info("Received score: {}: {}", name, points);
        }
        //these scores are sorted
        multiplayerScoresArray.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));
        //the list is cleared to be updated without any redundancies
        multiplayerScoreList.clear();
        multiplayerScoreList.addAll(multiplayerScoresArray);
    }

    /**
     * handles the respective messages send by the server
     * @param msg stores the message send by the communicator
     */
    public void handleMessages(String msg) {
        String[] parts = msg.split(" ", 2);
        if (parts[0].equals("SCORES")) {
            loadMultiplayerScores(parts[1]);
        }
        else if (parts[0].equals("NICK")) {
            username.set(parts[1]);
            leaderboard.getUsernameProperty().set(parts[1]);
        }
        else if(parts[0].equals("MSG")){
            sendChat(parts[1]);
        }
    }

    /**
     * handles sending a message and displaying it in chat
     * @param text holds the contents of the message and player
     */
    public void sendChat(String text) {
        String[] parts = text.split(":", 2);

        //The message is split into two parts
        //the user surrounded by a parenthesis and the message after that
        TextFlow message = new TextFlow();
        Text nickname = new Text("(" + parts[0] + ") ");
        Text contents = new Text(parts[1]);
        message.getChildren().addAll(nickname, contents);
        message.getStyleClass().add("messages");
        chat.getChildren().add(message);
        Multimedia.playAudio("message.wav");

        textFlows.add(message);

        //if the size of the messages exceeds 6 then
        //they are cleared to not take the whole screen
        if (textFlows.size() == 6){
            textFlows.clear();
        chat.getChildren().clear();
        //chat label is added to the VBox
        chat.getChildren().add(chatLabel);
    }
    }

    /**
     * same as the method in the challenge screen
     * @param key is the key that is pressed
     */
    public void pressedKey(KeyEvent key){
        if(key.getCode().equals(KeyCode.ESCAPE)){
            //only difference is that when a user leaves he is marked as dead
            leaderboard.removePlayer(username.get());
            communicator.send("DIE");
            game.stop();
            gameWindow.startLobby();
        }
        else if(key.getCode().equals(KeyCode.SPACE) || key.getCode().equals(KeyCode.R)){
            swap();
        }
        else if(key.getCode().equals(KeyCode.ENTER) || key.getCode().equals(KeyCode.X)){
            blockClicked(board.getBlock(rowPosition,columnPosition));
        }
        else if(key.getCode().equals(KeyCode.RIGHT) || key.getCode().equals(KeyCode.D)){
            if(rowPosition < game.getCols() - 1 ){
                rowPosition++;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        else if(key.getCode().equals(KeyCode.LEFT) || key.getCode().equals(KeyCode.A)){
            if(rowPosition > 0){
                rowPosition--;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        else if(key.getCode().equals(KeyCode.UP) || key.getCode().equals(KeyCode.W)){
            if(columnPosition > 0){
                columnPosition--;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }

        else if(key.getCode().equals(KeyCode.DOWN) || key.getCode().equals(KeyCode.S)){
            if(columnPosition < game.getRows() - 1 ){
                columnPosition++;
                board.getBlock(rowPosition,columnPosition).doHover();
            }
        }
        else if(key.getCode().equals(KeyCode.OPEN_BRACKET) || key.getCode().equals(KeyCode.Q) || key.getCode().equals(KeyCode.Z) ){
            rotate(3);
        }
        else if(key.getCode().equals(KeyCode.CLOSE_BRACKET) || key.getCode().equals(KeyCode.E) || key.getCode().equals(KeyCode.C) ){
            rotate(1);
        }
        else if(key.getCode().equals(KeyCode.T))
            text.setVisible(true);
    }
    public static void setMultiplayerGame(){
        multiplayerGame = true;
    }
}
