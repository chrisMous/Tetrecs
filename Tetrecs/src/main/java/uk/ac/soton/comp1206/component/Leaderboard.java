package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * The Leaderboard class extends the score list class that extends a Vbox
 * and handles the appearance and function of the online leaderboard
 */
public class Leaderboard extends ScoresList{
    //list property that holds the scores
    private final SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty();
    //string property that holds the username of the player
    private final StringProperty username = new SimpleStringProperty();
    //array list that stores the players that lost or exited the game
    private ArrayList<String> deadPlayers = new ArrayList();

    public Leaderboard(){
    //updates leaderboard when scores array is updated
    scores.addListener((ListChangeListener<? super Pair<String, Integer>>) e ->
            updateList()
    );
    //updates the username when a user changes nickname
        username.addListener(n -> updateList());

    setAlignment(Pos.CENTER);
    getStyleClass().add("scorelist");

}

    /**
     * handles the removal of a player that lost
     * @param player is the username of the player
     */
    public void removePlayer(String player){
        deadPlayers.add(player);
    }

    /**
     * updates the scores in real time and determines their appearance
     */
    public void updateList() {
        //the contents of the leaderboard are removed so it can be updated
        //without redundancies
        this.getChildren().clear();

        //all the pairs in the scores list are added to the leaderboard as an HBox
        for (Pair<String, Integer> score : scores) {
            HBox scoreBox = new HBox();
            scoreBox.getStyleClass().add("scoreitem");
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(10);

            //Add name and set its various styles depending on the user
            var name = new Text(score.getKey());
            if(username.get() != null && username.get().equals(name.getText())) {
                name.getStyleClass().add("myscore");
            }
            if(deadPlayers.contains(name.getText())){
                name.getStyleClass().add("deadscore");
            }
            else {
                name.getStyleClass().add("scorer");
            }
            name.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(name, Priority.ALWAYS);
            scoreBox.getChildren().add(name);

            //Add points
            var points = new Text(score.getValue().toString());
            points.getStyleClass().add("points");
            points.setTextAlignment(TextAlignment.CENTER);
            HBox.setHgrow(points,Priority.ALWAYS);
            scoreBox.getChildren().add(points);

            //Add score box to the leaderboard Vbox
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
