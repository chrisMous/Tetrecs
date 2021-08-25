package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The IntroScene class handles the animation that is displayed at the start of the app
 */
public class IntroScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(IntroScene.class);

    //a SequentialTransition is used to play the animations smoothly in order
    SequentialTransition transition;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public IntroScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Intro Scene");
        Multimedia.playMusic("intro.mp3");
    }

    /**
     * the user can press any key to skip the intro and go
     * directly to the menu screen
     */
    @Override
    public void initialise() {
        this.scene.setOnKeyPressed(k-> {
            transition.stop();
            gameWindow.startMenu();
        });

    }

    /**
     * handles the appearance and animation of the intro scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        var introPane = new BorderPane();
        introPane.setMaxWidth(gameWindow.getWidth());
        introPane.setMaxHeight(gameWindow.getHeight());
        root.getChildren().add(introPane);

        //text indicates that the scene can be skipped with any key
        //and this text is animated to fade in and out
        var text = new Text("- Press Any Key to Skip intro -");
        text.getStyleClass().add("intro");
        BorderPane.setAlignment(text,Pos.BOTTOM_CENTER);
        BorderPane.setMargin(text,new Insets(0,0,10,0));
        introPane.setBottom(text);
        text.setOpacity(0);
        FadeTransition fadeSkip = new FadeTransition(new Duration(750), text);
        fadeSkip.setToValue(1);
        FadeTransition fadeOut = new FadeTransition(new Duration(750), text);
        fadeOut.setToValue(0);
        SequentialTransition sequencialTransition = new SequentialTransition(fadeSkip,fadeOut);
        sequencialTransition.setCycleCount(Animation.INDEFINITE);
        sequencialTransition.play();

        //this stack contains the images associated with the animation
        var stack = new StackPane();
        introPane.setCenter(stack);

        //image that displays the ECSGames logo
        ImageView logo = new ImageView(Multimedia.getImage("ECSGames.png"));
        logo.setPreserveRatio(true);
        logo.setFitWidth(gameWindow.getWidth()/2);
        logo.setOpacity(0);
        stack.getChildren().add(logo);

        //gridPane that holds the letters that amke up ECS
        GridPane gridPane = new GridPane();
        stack.getChildren().add(gridPane);
        stack.setAlignment(Pos.CENTER);
        gridPane.setAlignment(Pos.CENTER);
        StackPane.setAlignment(gridPane, Pos.CENTER);

        //image for the letter e and animation to enlarge it by a scale factor of 2
        //and make it smaller again
        ImageView e = new ImageView(Multimedia.getImage("e.png"));
        gridPane.add(e,1,0);
        ScaleTransition scaleE = new ScaleTransition();
        scaleE.setNode(e);
        scaleE.setDuration(Duration.millis(600));
        scaleE.setCycleCount(2);
        scaleE.setInterpolator(Interpolator.LINEAR);
        scaleE.setByX(2);
        scaleE.setByY(2);
        scaleE.setAutoReverse(true);
        e.setOpacity(0);

        //image for the letter c and animation to enlarge it by a scale factor of 2
        //and make it smaller again
        ImageView c = new ImageView(Multimedia.getImage("c.png"));
        gridPane.add(c,2,0);
        ScaleTransition scaleC = new ScaleTransition();
        scaleC.setNode(c);
        scaleC.setDuration(Duration.millis(600));
        scaleC.setCycleCount(2);
        scaleC.setInterpolator(Interpolator.LINEAR);
        scaleC.setByX(2);
        scaleC.setByY(2);
        scaleC.setAutoReverse(true);
        c.setOpacity(0);

        //image for the letter s and animation to enlarge it by a scale factor of 2
        //and make it smaller again
        ImageView s = new ImageView(Multimedia.getImage("s.png"));
        gridPane.add(s,3,0);
        ScaleTransition scaleS = new ScaleTransition();
        scaleS.setNode(s);
        scaleS.setDuration(Duration.millis(600));
        scaleS.setCycleCount(2);
        scaleS.setInterpolator(Interpolator.LINEAR);
        scaleS.setByX(2);
        scaleS.setByY(2);
        scaleS.setAutoReverse(true);
        s.setOpacity(0);

        //fadesIn the e letter
        FadeTransition fadeE = new FadeTransition(new Duration(300), e);
        fadeE.setToValue(1);
        //fadesIn the c letter
        FadeTransition fadeC = new FadeTransition(new Duration(100), c);
        fadeC.setToValue(1);
        //fadesIn the s letter
        FadeTransition fadeS = new FadeTransition(new Duration(100), s);
        fadeS.setToValue(1);
        //fadesOut the grid containing "ECS"
        FadeTransition fadeGrid= new FadeTransition(new Duration(200), gridPane);
        fadeGrid.setToValue(0);
        //fadesIn the ECSGames logo
        FadeTransition fadeLogo = new FadeTransition(new Duration(1500), logo);
        fadeLogo.setToValue(1);

        //transition that handles the animation in order
        transition = new SequentialTransition(fadeE,scaleE,fadeC,scaleC,fadeS,scaleS,fadeGrid,fadeLogo);
        transition.play();
        logger.info("Starting transition");

        //after the animation is over the user is taken to the menu screen
        transition.setOnFinished(t -> gameWindow.startMenu());
    }
}
