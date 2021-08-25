package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The InstructionsScene class displays the instructions screen that holds details about
 * the rules and controls of the game and its game pieces
 */
public class InstructionsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * when escape is pressed the user is taken back to the menu
     */
    @Override
    public void initialise() {
        this.scene.setOnKeyPressed(this::pressedKey);
    }
    public void pressedKey(KeyEvent key){
        if(key.getCode().equals(KeyCode.ESCAPE)){
            gameWindow.startMenu();
        }
    }

    /**
     * handles the appearance of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        var InstructionsPane = new StackPane();
        InstructionsPane.setMaxWidth(gameWindow.getWidth());
        InstructionsPane.setMaxHeight(gameWindow.getHeight());
        InstructionsPane.getStyleClass().add("menu-background");
        root.getChildren().add(InstructionsPane);

        var mainPane = new BorderPane();
        InstructionsPane.getChildren().add(mainPane);

        //VBox that holds the contents of the top part of the scene
        var topPart = new VBox();
        BorderPane.setAlignment(topPart, Pos.CENTER);
        topPart.setAlignment(Pos.TOP_CENTER);
        mainPane.setTop(topPart);

        //label that serves as the heading for the instructions
        var instructionsLabel = new Text("Instructions");
        instructionsLabel.getStyleClass().add("heading");
        topPart.getChildren().add(instructionsLabel);

        //text that displays how you lose the game and ways to delay losing
        var instructionsText = new Text("Lose 3 Lives and you're out! Clear lines if you don't want to end up in the GAME OVER Screen!");
        instructionsText.getStyleClass().add("instructions");
        instructionsText.setTextAlignment(TextAlignment.CENTER);
        topPart.getChildren().add(instructionsText);

        //image that holds the rules nad controls of the game
        ImageView instructionsImage = new ImageView(Multimedia.getImage("Instructions.png"));
        mainPane.setCenter(instructionsImage);
        instructionsImage.setPreserveRatio(true);
        instructionsImage.setFitWidth(gameWindow.getHeight());

        //VBox that holds the components of the bottom part of the scene
        var bottomPart = new VBox();
        BorderPane.setAlignment(bottomPart, Pos.CENTER);
        bottomPart.setAlignment(Pos.BOTTOM_CENTER);
        mainPane.setBottom(bottomPart);

        //label that indicates the pieces below it
        var piecesLabel = new Text("Pieces");
        piecesLabel.getStyleClass().add("heading");
        bottomPart.getChildren().add(piecesLabel);

        //grid pane that holds the pieces to be displayed
        GridPane piecesPane = new GridPane();
        bottomPart.getChildren().add(piecesPane);
        piecesPane.setAlignment(Pos.BOTTOM_CENTER);
        piecesPane.setVgap(12);
        piecesPane.setHgap(12);

        int x=0;
        int y=0;

        //there are 15 pieces to be displayed and they are displayed in a PieceBoard grid
        for(int i = 0; i<15; i++){
            GamePiece gamePiece = GamePiece.createPiece(i);
            PieceBoard pieceBoard = new PieceBoard(this.gameWindow.getWidth()/13,this.gameWindow.getHeight()/13);
            pieceBoard.displayPiece(gamePiece);
            piecesPane.add(pieceBoard,x,y);
            x++;

            //if the x value reaches 5 the y value is incremented and x value set to 0
            //so that the pieces are displayed evenly in the screen
            if(x == 5){
                y++;
                x=0;
            }
        }
    }
}
