package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The ScoresScene class handles displaying the high scores at the end of a game
 */
public class ScoresScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final Communicator communicator;
    protected Game game;

    //hold the scores to be displayed
    private ObservableList<Pair<String, Integer>> observableList;
    private static ArrayList<Pair<String, Integer>> scoreLine;

    //ScoreList that shows the offline scores
    private ScoresList scoreList;
    //ScoreList that shows the online scores
    private ScoresList remoteScoreList;

    //holds the username of the player
    private SimpleStringProperty username = new SimpleStringProperty();

    //hold the remote scores to be displayed
    private ObservableList<Pair<String, Integer>> remoteScores;
    private ArrayList<Pair<String, Integer>> remoteScoresArray = new ArrayList();

    //determines whether a score is a highScore or not
    private boolean isHighScore = false;
    private boolean isOnlineHighScore = false;

    private VBox topPart;

    //holds the scores of the players
    private ArrayList<Integer> score = new ArrayList();

    TextField nameField;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        logger.info("Creating Scores Scene");
        this.game = game;
        communicator = gameWindow.getCommunicator();
    }

    @Override
    public void initialise() {
        Multimedia.playMusic("end.wav");

        //the communicator listens to messages and calls the
        //loadOnlineScores method to display scores
        communicator.addListener(message -> {
            if (!message.startsWith("HISCORES")) {
                return;
            }
            Platform.runLater(() -> this.loadOnlineScores(message));
        });

        communicator.send("HISCORES");

        //listens to when a key is pressed and calls the pressedKey method
        this.scene.setOnKeyPressed(this::pressedKey);
    }

    public void pressedKey(KeyEvent key){
        if(key.getCode().equals(KeyCode.ESCAPE)){
            gameWindow.startMenu();
        }
    }

    /**
     * Builds the appearance of the Scores scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        var endPane = new StackPane();
        endPane.setMaxWidth(gameWindow.getWidth());
        endPane.setMaxHeight(gameWindow.getHeight());
        endPane.getStyleClass().add("menu-background");
        root.getChildren().add(endPane);

        var mainPane = new BorderPane();
        endPane.getChildren().add(mainPane);

        topPart = new VBox(5);
        BorderPane.setAlignment(topPart, Pos.CENTER);
        topPart.setAlignment(Pos.TOP_CENTER);
        mainPane.setTop(topPart);

        //image of the logo of the game
        ImageView titleImage = new ImageView(Multimedia.getImage("TetrECS.png"));
        mainPane.setCenter(titleImage);
        titleImage.setPreserveRatio(true);
        titleImage.setFitWidth(gameWindow.getHeight());
        topPart.getChildren().add(titleImage);

        //label that indicates that the agem is over
        var gameOverLabel = new Text("GAME OVER");
        gameOverLabel.getStyleClass().add("bigtitle");
        topPart.getChildren().add(gameOverLabel);

        var scoresLabel = new Text("High-Score List");
        scoresLabel.getStyleClass().add("heading");
        topPart.getChildren().add(scoresLabel);

        //grid that holds offline and online scores
        GridPane scoresGrid = new GridPane();
        scoresGrid.setAlignment(Pos.CENTER);
        scoresGrid.setHgap(100.0);
        mainPane.setCenter(scoresGrid);

        Text offlineScoresLabel;
        if(MultiplayerScene.multiplayerGame) {
            offlineScoresLabel = new Text("This Game");
        }
        else {
            offlineScoresLabel = new Text("Offline Scores");
        }
        offlineScoresLabel.getStyleClass().add("heading");
        scoresGrid.add(offlineScoresLabel,0,0);

        //scoresList for offline scores
        scoreList = new ScoresList();
        GridPane.setHalignment(scoreList, HPos.CENTER);
        scoresGrid.add(scoreList, 0, 1);

        var onlineScoresLabel = new Text("Online Scores");
        onlineScoresLabel.getStyleClass().add("heading");
        scoresGrid.add(onlineScoresLabel,1,0);

        //scoresList for online scores
        remoteScoreList = new ScoresList();
        GridPane.setHalignment(remoteScoreList, HPos.CENTER);
        scoresGrid.add(remoteScoreList, 1, 1);

        //if the game doesn't have any scores preloaded ones are displayed
        if (game.getScores().size() == 0) {
            observableList = FXCollections.observableArrayList(loadScores());
        } else {
            observableList = FXCollections.observableArrayList(game.getScores());

        }

        //holds the offline scores to be displayed and updates them
        SimpleListProperty<Pair<String, Integer>> leaderboard = new SimpleListProperty(observableList);
        scoreList.scoreProperty().bind(leaderboard);
        scoreList.getUsernameProperty().bind(username);

        //holds the online scores to be displayed and updates them
        remoteScores = FXCollections.observableArrayList(remoteScoresArray);
        SimpleListProperty<Pair<String, Integer>> onlineLeaderboard = new SimpleListProperty(remoteScores);
        remoteScoreList.scoreProperty().bind(onlineLeaderboard);
        remoteScoreList.getUsernameProperty().bind(username);
    }

    /**
     * method that loads the offline scores to be displayed
     * @return the offline scores to be displayed
     */
    public static ArrayList<Pair<String, Integer>> loadScores() {
        logger.info("Loading scores from file");

        scoreLine = new ArrayList();

        try {
            //scores are located in the "highScores.txt" file
            Path path = Paths.get("highScores.txt");

            //if the file does not exist the some scores are generated for show
            if (Files.notExists(path)) {
                for (int i = 0; i < 10; i++) {
                    scoreLine.add(new Pair("Player"  , 2500 - (i * 250)));
                }
                writeScores(scoreLine);
            }
            //all of the lines of the file are read
            List<String> data = Files.readAllLines(path);

            //scores are separated by username and score and stored in the ArrayList
            for (String score : data) {
                String[] parts = score.split(":");
                int amount = Integer.parseInt(parts[1]);
                scoreLine.add(new Pair(parts[0], amount));
            }

        } catch (Exception e) {
            logger.error("Unable to read from file!");
            e.printStackTrace();
        }

        return scoreLine;
    }

    /**
     * writes the offline scores into a file
     * @param scoreLine is the arraylist containing the scores
     */
    public static void writeScores(ArrayList<Pair<String, Integer>> scoreLine) {
        //the arrayList is sorted so the scores appear in descending order
        scoreLine.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));

        try {
            //a file is created and scores written into it
            File file = new File("highScores.txt");
            Writer writer = new FileWriter(file);

            int counter = 0;

            for (Pair<String, Integer> score : scoreLine) {
                counter++;

                //scores are separated by name and score value
                writer.write(score.getKey() + ":" + score.getValue() + "\n");

                //only 10 scores are displayed
                if (counter >= 10)
                    break;
            }

            writer.close();

        } catch (IOException e) {
            logger.error("Unable to write file!");
            e.printStackTrace();
        }
    }

    /**
     * This method loads the online scores and also
     * updates the offline score list if a new high score is set
     * @param message
     */
    public void loadOnlineScores(String message) {

        //all scores are split into two parts the username and scoreValue
        for (String score :  message.split("\n")) {
            String[] parts = score.split(":");
            if (parts.length < 2) continue;
            String name = parts[0];
            String points = parts[1];

            //if the name part contains "HIGHSCORES"
            if (name.contains("HISCORES")){
                //then the text is replaced with nothing so that the score can be shown
                name = name.replace("HISCORES ", "");
        }
            //scores are added in the array list
            remoteScoresArray.add(new Pair(name, Integer.valueOf(points)));
            logger.info("Received score: {}: {}",  name,  points);
        }
        //online scores are sorted
        remoteScoresArray.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));
        //the previous list is cleared
        remoteScores.clear();
        //the remote scores are now displayed with the users score in them
        remoteScores.addAll(remoteScoresArray);

        //the users score is stored
        int myScore = game.getScore().get();

        //it's added in the arrayList
        for(Pair<String, Integer> pair : scoreLine) {
            score.add(pair.getValue());
        }

            //if the score is higher than the lowest score then it's added in the list
            if(myScore > this.score.get(9)){
                nameField = new TextField();
                nameField.setPrefWidth((this.gameWindow.getWidth() / 3));
                //the user is prompted to enter the name he wants displayed next to his score
                nameField.setPromptText("Enter your name");
                topPart.getChildren().add( nameField);

                //the button to submit the score
                Button sumbitName = new Button("Submit");
                topPart.getChildren().add(sumbitName);

                //when the button is pressed the users score is added into the list
                sumbitName.setOnAction(e -> {
                    String username = nameField.getText();
                    scoreLine.add(new Pair(username,myScore));

                    observableList.add(new Pair(username,myScore));
                    //the list is sorted to correctly display the score
                    observableList.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));
                    //the last element of the list is removed to have only 10 scores
                    observableList.remove(10);

                    nameField.setVisible(false);
                    sumbitName.setVisible(false);

                    //the scores are written in the scoresFile
                    writeScores(scoreLine);

                });

                //method that checks if the user beat any online high scores
                    writeOnlineScore();

            }
    }

    /**
     * when user gets an online high score then his score is displayed
     */
    public void writeOnlineScore(){
        int myScore = game.getScore().get();

        //all of the scores are checked
        for(Pair<String,Integer> pair: remoteScores){
           int score = pair.getValue();
           //if the users score is higher than a score in the list
           if(myScore > score){
               isOnlineHighScore = true;

               //the score is added in the array and then sorted
               remoteScoresArray.add(new Pair(nameField.getText(),myScore));
               remoteScoresArray.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));
               //the previous list is cleared
               remoteScores.clear();
               //the remote scores are now displayed with the users score in them
               remoteScores.addAll(remoteScoresArray);

               communicator.send("HISCORE " + nameField.getText() + ":" + myScore);
               this.communicator.send("HISCORES");
               return;
           }
        }
    }

}
