package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

/**
 * The ScoresList class extends the VBox object and handles the appearance and
 * function of the offline scores list that appears when the game is over
 */
public class ScoresList extends VBox {
    //list property that holds the scores
    private final SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty();

    //string property that holds the username of the player
    private final StringProperty username = new SimpleStringProperty();

    public ScoresList() {
        //updates score list when scores are updated
       scores.addListener((ListChangeListener<? super Pair<String, Integer>>)e ->
                updateList()
       );

        setAlignment(Pos.CENTER);
        getStyleClass().add("scorelist");

    }

    /**
     * updates the scores in real time and determines their appearance
     */
    public void updateList() {
        //the contents of the scores list are removed so it can be updated
        //without redundancies
        this.getChildren().clear();

        //all the pairs in the scores list are added to the scores list as an HBox
        for (Pair<String, Integer> score : scores) {
            HBox scoreBox = new HBox();
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.getStyleClass().add("scorelist");
            scoreBox.setSpacing(10);

            //Add name ans set is respective appearance
            var name = new Text(score.getKey());
            if(username.get() != null && username.get().equals(name.getText())) {
                name.getStyleClass().add("myscore");
            }
            else {
                name.getStyleClass().add("scorer");
            }
            name.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(name, Priority.ALWAYS);
            scoreBox.getChildren().add(name);

            //Add points and style them
            var points = new Text(score.getValue().toString());
            points.getStyleClass().add("points");
            points.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(points,Priority.ALWAYS);
            scoreBox.getChildren().add(points);

            //Add score box to the score list object
            getChildren().add(scoreBox);

        }

    }

    /**
     * @return the scores array
     */
    public ListProperty<Pair<String, Integer>> scoreProperty() {
        return this.scores;
    }

    /**
     * @return the username of the player
     */
    public StringProperty getUsernameProperty() {
        return this.username;
    }
}
