package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    //boolean value that determines the centre of the circle
    private boolean centre = false;
    //boolean value that determines if a block should be hovered
    private boolean hover;

    private AnimationTimer timer;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //When the mouse is over the block, hover
        setOnMouseEntered((e) -> {
            hover = true;
            paint();
        });

        //When the mouse leaves, no longer hover
        setOnMouseExited((e) -> {
            hover = false;
            paint();
        });

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        //if it's the centre of the block draw a shape
        if(centre)
        centreBlock();

        //if you are hovering the block perform the hover effect
        if(hover) {
           doHover();
        }

    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.TRANSPARENT);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0,width,height);

        //Shapes
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(2.5,2.5,width-5,height-5,10,10);
        gc.strokeLine(2.5,2.5,width-3,height-3);
        gc.strokeLine(2.5,height - 3, width -3 ,2.5);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * draws a circle on the centre of a block to highlight it
     */
    public void centreBlock() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1, 0.6));
        gc.fillOval(width / 4, height / 4, width / 2, height / 2);
    }

    /**
     * sets the centre of the circle
     * @param centre boolean value that is true when the centre is found out
     */
    public void setCentre(Boolean centre){
        this.centre = centre;
        centre = true;
    }

    /**
     * handles the hovering effect on the block
     */
    public void doHover(){
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.fillRect(0, 0, width, height);
    }

    /**
     *  starts the animation timer that enables a block to fade out
     */
    public void fadeOut(){
        timer = new MyTimer();
        timer.start();
    }

    /**
     *  custom AnimationTimer class that handles the fading out animation of a block
     *  when a line is cleared
     */
    private class MyTimer extends AnimationTimer{

        //opacity is initially set to 1 so full opacity
        private double opacity = 1 ;
        @Override
        public void handle(long l) {
                fadeOut();
        }

        /**
         * the fade out method gradually fades out the block from 1 to 0
         * decreasing its opacity value by -0.03 each time untill it's 0
         */
        private void fadeOut(){
           paintEmpty();
            opacity -= 0.03;

            //when the opacity reaches 0 the animation stops
            if(this.opacity <= 0){
                stop();
                timer = null;
                return;
            }
            GraphicsContext gc = getGraphicsContext2D();
            gc.setFill(Color.color(0, 1, 0, this.opacity));
            gc.fillRect(0, 0, width, height);
        }
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
