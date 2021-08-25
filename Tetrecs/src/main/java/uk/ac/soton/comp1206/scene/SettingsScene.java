package uk.ac.soton.comp1206.scene;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The SettingsScene class includes a simple settings screen that allows
 * customizable single player themes and volume sliders for the music and
 * sound effects
 */
public class SettingsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(SettingsScene.class);
    //slider for the soundFX
    private Slider mediaSlider;
    //slider for the background music
    private Slider musicSlider;
    //default style set to the challenge-background that the css file contains
    private static String style = "challenge-background";

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public SettingsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Settings Scene");
    }

    @Override
    public void initialise() {
        //listens when a user presses a key and calls the in class method
        this.scene.setOnKeyPressed(this::pressedKey);

        //music and media slider set to max volume at the start
        musicSlider.setValue(Multimedia.getMusicVolume() * 100);
        mediaSlider.setValue(Multimedia.getMediaVolume() * 100);

        //listens to changes in the music slider and sets the volume
        //according to the location the user slides to
        musicSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                    Multimedia.setMusicVolume(musicSlider.getValue() / 100);
            }
        });
        //listens to changes in the media slider and sets the soundfx volume
        //according to the location the user slides to
       mediaSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Multimedia.setMediaVolume(mediaSlider.getValue() / 100);
            }
        });
    }

    /**
     * builds the appearance of the settings class and handles the theme selection
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var settingsPane = new BorderPane();
        settingsPane.setMaxWidth(gameWindow.getWidth());
        settingsPane.setMaxHeight(gameWindow.getHeight());
        settingsPane.getStyleClass().add("settings");
        root.getChildren().add(settingsPane);

        //title for the settings screen
        var settingsLabel = new Text("Settings");
        settingsLabel.getStyleClass().add("bigtitle");
        settingsLabel.setTextAlignment(TextAlignment.CENTER);
        BorderPane.setAlignment(settingsLabel, Pos.TOP_CENTER);
        settingsPane.setTop(settingsLabel);

        //VBox that contains the sliders and positions them bottom-centre of the screen
        var bottomPart = new VBox();
        bottomPart.setPrefWidth(gameWindow.getWidth() / 3);
        bottomPart.setFillWidth(false);
        bottomPart.setAlignment(Pos.BOTTOM_CENTER);
        BorderPane.setAlignment(bottomPart,Pos.BOTTOM_CENTER);
        bottomPart.setPadding(new Insets(5,5,5,5));
        settingsPane.setBottom(bottomPart);

        //label and slider for the background music
        var musicSliderLabel = new Text("Music Slider");
        musicSliderLabel.getStyleClass().add("heading");
        bottomPart.getChildren().add(musicSliderLabel);
        musicSlider = new Slider();
        musicSlider.setPrefWidth(gameWindow.getWidth() / 3);
        bottomPart.getChildren().add(musicSlider);

        //label and slider for the sound effects
        var mediaSliderLabel = new Text("SoundFX Slider");
        mediaSliderLabel.getStyleClass().add("heading");
        bottomPart.getChildren().add(mediaSliderLabel);
        mediaSlider = new Slider();
        mediaSlider.setPrefWidth(gameWindow.getWidth() / 3);
        bottomPart.getChildren().add(mediaSlider);

        //grid that contains the images of the themes that can be applied
        var imageGrid = new GridPane();
        BorderPane.setAlignment(imageGrid, Pos.CENTER);
        imageGrid.setAlignment(Pos.CENTER);
        settingsPane.setCenter(imageGrid);
        imageGrid.setHgap(12);
        imageGrid.setVgap(12);
        imageGrid.getStyleClass().add("gameBox");

        var imagesLabel = new Text("Choose Single Player Theme");
        GridPane.setHalignment(imagesLabel, HPos.CENTER);
        imagesLabel.getStyleClass().add("heading");
        imageGrid.add(imagesLabel,0,0,3,1);

        //images one to six are placed on the grid
        ImageView one = new ImageView(Multimedia.getImage("1.jpg"));
        one.setFitHeight(170);
        one.setFitWidth(200);
        imageGrid.add(one,0,1);
        ImageView two = new ImageView(Multimedia.getImage("2.jpg"));
        imageGrid.add(two,1,1);
        two.setFitHeight(170);
        two.setFitWidth(200);
        ImageView three = new ImageView(Multimedia.getImage("3.jpg"));
        imageGrid.add(three,2,1);
        three.setFitHeight(170);
        three.setFitWidth(200);
        ImageView four = new ImageView(Multimedia.getImage("4.jpg"));
        four.setFitHeight(170);
        four.setFitWidth(200);
        imageGrid.add(four,0,2);
        ImageView five = new ImageView(Multimedia.getImage("5.jpg"));
        imageGrid.add(five,1,2);
        five.setFitHeight(170);
        five.setFitWidth(200);
        ImageView six = new ImageView(Multimedia.getImage("6.jpg"));
        imageGrid.add(six,2,2);
        six.setFitHeight(170);
        six.setFitWidth(200);

        //when the user selects a picture the theme is applied
        //to his single player challenge
       one.setOnMouseClicked(e->{
            Multimedia.playAudio("place.wav");
           style = "menu-background";
           logger.info("Set single player theme to 1");
        } );
        two.setOnMouseClicked(e->{
            Multimedia.playAudio("place.wav");
            style = "challenge-background2";
            logger.info("Set single player theme to 2");
        } );
        three.setOnMouseClicked(e->{
            Multimedia.playAudio("place.wav");
            style = "challenge-background3";
            logger.info("Set single player theme to 3");
        } );
        four.setOnMouseClicked(e->{
            Multimedia.playAudio("place.wav");
            style = "lobby-background";
            logger.info("Set single player theme to 4");
        } );
        five.setOnMouseClicked(e->{
            Multimedia.playAudio("place.wav");
            style = "multiplayer-background";
            logger.info("Set single player theme to 5");
        } );
        six.setOnMouseClicked(e->{
            Multimedia.playAudio("place.wav");
            style = "challenge-background";
            logger.info("Set single player theme to 6");
        } );
    }


    /**
     * @return the String of the corresponding style selected
     */
    public static String style(){
        return style;
    }

    /**
     * when the user presses escape he is sent back to the menu
     * @param key holds the key pressed
     */
    public void pressedKey(KeyEvent key) {
        if (key.getCode().equals(KeyCode.ESCAPE)) {
            gameWindow.startMenu();
        }
    }
}