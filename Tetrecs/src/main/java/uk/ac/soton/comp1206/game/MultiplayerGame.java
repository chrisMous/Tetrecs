package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;


import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class MultiplayerGame extends Game {
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private Communicator communicator;

    //linked list that stores the queued pieces
    private Queue<GamePiece> pieceQueue = new LinkedList<>();
    //checks whether the first pieces were placed
    protected boolean initialisedPieces = false;
    /**
     * Create a new multiplayer game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;

        // the communicator listens to any messages send
        communicator.addListener(message -> {
            Platform.runLater(() -> this.getMessage(message));
        });
    }

    /**
     * initialises the multiplayer game
     */
    public void initialiseGame() {
        logger.info("Initialising Multiplayer Game");
       Multimedia.playMusic("game.wav");
       //send the initial pieces for the game to start
        for (int i = 0; i < 10; i++)
            this.communicator.send("PIECE");
    }

    /**
     * initialises and queues the pieces specified from the server
     * @param piece stores the type of piece to be placed
     */
    public void getPieces(int piece) {
        GamePiece gamePiece = GamePiece.createPiece(piece);
        //queues the gamePiece by adding it in the queue
        pieceQueue.add(gamePiece);
        logger.info("Queued piece: {}", gamePiece);

        // if the current and next pieces were queued and the starting pieces were not initialised
        // the next piece is fetched and the pieces are marked as initialised
        if(pieceQueue.size() > 2 && !initialisedPieces) {
            followingPiece = spawnPiece();
            nextPiece();
            initialisedPieces = true;
        }
    }

    /**
     * spawns a game piece and sends a message to the server
     * @return the first piece stored in the piece queue
     */
    public GamePiece spawnPiece() {
        communicator.send("PIECE");
        logger.info("Spawning Piece!");

        //dequeues the first piece stored in the queue
        return pieceQueue.poll();
    }

    /**
     * handles the respective messages send by the server
     * @param msg stores the message send by the communicator
     */
    public void getMessage(String msg) {
        //message is split into two parts, the command and its contents
        String[] parts = msg.split(" ", 2);
        if (parts[0].equals("SCORES")) {
            handleScores(parts[1]);
        }
        else if(parts[0].equals("PIECE")){
            int piece = Integer.parseInt(parts[1]);
            getPieces(piece);
        }
    }

    /**
     * handles the scores send by the server
     * @param message stores the message containing the score data
     */
    public void handleScores(String message) {
        this.scores.clear();
        logger.info("Received scores: {}", (Object) message);
        //the scores are split by line and by the : that separates the name and score
        for (String score : message.split("\n")) {
            String[] parts = score.split(":");
            //if the parts are less than two they are ignored
            if (parts.length < 2) continue;
            String name = parts[0];
            String points = parts[1];

            //scores are added to the scores array in order to by displayed later
            this.scores.add(new Pair(name, Integer.valueOf(points)));
            logger.info("Received score: {}: {}", name, points);
        }
        //the scores array is sorted
        this.scores.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));
    }

}
