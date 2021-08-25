package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */


    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");

    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //logo Image set on the Top Right of teh screen
        ImageView logoImage = new ImageView(Multimedia.getImage("ECSGames.png"));
        mainPane.setTop(logoImage);
        logoImage.setFitWidth(150);
        logoImage.setFitHeight(150);
        logoImage.setPreserveRatio(true);

        //TETRECS logo with an animation of it swinging
        ImageView titleImage = new ImageView(Multimedia.getImage("TetrECS.png"));
        mainPane.setCenter(titleImage);
        titleImage.setPreserveRatio(true);
        titleImage.setFitWidth(gameWindow.getHeight());
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(5), titleImage);
        rotateTransition.setFromAngle(-7);
        rotateTransition.setToAngle(7);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();

        //Vbox that contains the buttons that a player can click for navigation
        var menu = new VBox();
        BorderPane.setAlignment(menu, Pos.CENTER);
        menu.setAlignment(Pos.BOTTOM_CENTER);
        mainPane.setBottom(menu);

        var singlePlayerButton = new Button("Single Player");
        singlePlayerButton.getStyleClass().add("menuItem");
        menu.getChildren().add(singlePlayerButton);

        var multiPlayerButton = new Button(" Multi-Player ");
        multiPlayerButton.getStyleClass().add("menuItem");
        menu.getChildren().add(multiPlayerButton);

        var instructionsButton = new Button("How-To-play");
        instructionsButton.getStyleClass().add("menuItem");
        menu.getChildren().add(instructionsButton);

        var exitButton = new Button("        EXIT        ");
        exitButton.getStyleClass().add("menuItem");
        menu.getChildren().add(exitButton);

        //Bind the button action to the startGame method in the menu
        singlePlayerButton.setOnAction(this::startGame);

        //when exitButton is pressed the app stops
        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                App.getInstance().shutdown();
            }
        });
        //how-to-play button opens the InstructionsScene
        instructionsButton.setOnAction(this::startInstructions);

        //multiplayer button opens the multiplayer lobby
        multiPlayerButton.setOnAction(this::startLobby);

        //when the ECS logo is pressed the user is taken to the settings
        logoImage.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            gameWindow.startSettings();
        });

    }


    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        Multimedia.playMusic("menu.mp3");
        this.scene.setOnKeyPressed(this::pressedKey);
    }

    /**
     * when escape is pressed the app shuts down
     */
    public void pressedKey(KeyEvent key){
        if(key.getCode().equals(KeyCode.ESCAPE)){
            App.getInstance().shutdown();
        }
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }
    /**
     * Handle when the How-to-play button is pressed
     * @param event event
     */
    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructions();
    }

    /**
     * Handle when the Multi-Player button is pressed
     * @param event event
     */
    private void startLobby(ActionEvent event){
        gameWindow.startLobby();
    }
}
