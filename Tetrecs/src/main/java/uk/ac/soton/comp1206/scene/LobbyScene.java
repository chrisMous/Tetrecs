package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utilities.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The LobbyScene class handles the functionality and appearance of the
 * multiplayer lobby users can create or join channels as well as type in the chat
 * of channel they joined. It's also possible to leave a channel or start a game if you are the host
 */
public class LobbyScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private final Communicator communicator;

    //timer is used to request messages from the server periodically
    private Timer timer;
    //holds the list of channels
    private ArrayList<String> channelList = new ArrayList<>();
    //holds the users in a channel
    private ArrayList<String> nameList = new ArrayList<>();
    private TextField text;
    //holds the messages that are in the chat
    private VBox messages;

    //prompts user to type a channel
    private TextField channelField;
    //contains the list of channels that are displayed
    private HBox channelsBox;
    //contains the list of players that are displayed
    private HBox playersList;

    Button startGame;

    //determines whether a player is the host or not
    private final BooleanProperty host = new SimpleBooleanProperty();
    //hols the channel name
    private StringProperty channel = new SimpleStringProperty();
    //holds the username
    private StringProperty name = new SimpleStringProperty();

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
        communicator = gameWindow.getCommunicator();
    }

    @Override
    public void initialise() {
        Multimedia.playMusic("menu.mp3");
        //listens to when a key is pressed and executes the in class method
        this.scene.setOnKeyPressed(this::pressedKey);

        //TimerTask that is used to send LIST messages to the server requesting
        //a channel list
        TimerTask requestChannels = new TimerTask() {
            @Override
            public void run() {
               communicator.send("LIST");
               logger.info("Requesting Channels");
            }
        };
        //communicator listens to incoming messages nad calls the handleCommans method
        //taking the message as a parameter
        communicator.addListener(message ->{
            Platform.runLater(() -> this.handleCommands(message));
        });
        timer = new Timer();

        //timer is scheduled to request channels every 500MS
        timer.schedule(requestChannels,0,5000);
    }

    /**
     * Handles commands and messages that are retrieved form the server
     * @param command holds the contents of the message
     */
    public void handleCommands(String command){
        logger.info("Received Command {}", command);
        //the mesagess are split into two parts separated by a space
        String[] messageReceived = command.split(" ",2);


        switch (messageReceived[0]) {
            //if the first part of the message contains the string "CHANNELS"
            case "CHANNELS":
                //if it's empty the channels list is cleared
                if (messageReceived[1].isEmpty()) {
                    channelsBox.getChildren().clear();
                    logger.info("No Channels Available");
                    return;
                } else {
                    //if it's not empty a method is called
                    getChannelList(messageReceived[1]);
                }
                break;
            //if the first part of the message contains the string "HOST"
            case "HOST":
                //user becomes host
                host.set(true);
                //startGame button is displayed as only host can start a game
                startGame.setVisible(true);
                logger.info("Promoted to host!");
                break;
            //if the first part of the message contains the string "JOIN"
            case "JOIN":
                //user joins the session contained on the second part of the string
                joinSession(messageReceived[1]);
                startGame.setVisible(false);
                logger.info("Joined {}", messageReceived[1]);
                break;
            //if the first part of the message contains the string "PARTED"
            case "PARTED":
                //user left channel and channel is set to empty
                channel.set("");
                logger.info("Parted from Channel");
                break;
            //if the first part of the message contains the string "USERS"
            case "USERS":
                //get players method is called
                getPlayers(messageReceived[1].replace(" ", ""));
                break;
            //if the first part of the message contains the string "NICK"
            case "NICK":
                //name of the player is set to teh second part of the string
                name.set(messageReceived[1]);
                break;
            //if the first part of the message contains the string "START"
            case "START":
                //the multiplayer game starts
                gameWindow.startMultiplayer();
                break;
            //if the first part of the message contains the string "MSG"
            case "MSG":
                //the message contained in the second part is displayed
                displayMessage(messageReceived[1]);
                break;
            //if the first part of the message contains the string "ERROR"
            case "ERROR":
                //an alert is displayed with the alert message contained in the second part of the screen
                String errorMessage = messageReceived[1];
                logger.error(errorMessage);
                Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
                alert.showAndWait();
                break;
        }
    }

    /**
     * handles the creation of a channel and reports this event to the server
     */
    public void createChannel(){
        String channelName = channelField.getText();
        communicator.send("CREATE " + channelName);
    }
    /**
     * handles joining a channel
     */
    public void joinChannel(){
        communicator.send("JOIN " + channelField.getText() );
    }

    /**
     * displays the channels on the screen
     * @param lists contains the message containing the channels
     */
    public void getChannelList(String lists) {
        channelList.clear();
        channelsBox.getChildren().clear();

        logger.info("Received channel list: {}", lists);
        String[] channels = lists.split("\n");
        channelList.addAll(Arrays.asList(channels));

        //all the channels are displayed as text
        for(String channel : channelList){
            Text channelName = new Text(channel);
            channelName.getStyleClass().add("channelItem");
            channelsBox.getChildren().add(channelName);
        }
       }

    /**
     * handles joining a channel session
     * @param channel is the channel to join
     */
    public void joinSession(String channel){
        logger.info("Joining channel {}",channel);
        channelList.add(channel);
       }

    /**
     * displays all of the players inside a channel
     * @param users is the string containing the name of the user
     */
       public void getPlayers(String users){
        nameList.clear();
        playersList.getChildren().clear();
        String[] names = users.split("\n");
        nameList.addAll(Arrays.asList(names));

        //users name is displayed as text
        for(String name : nameList){
            Text userName = new Text(name);
            playersList.getChildren().add(userName);
        }
       }

    /**
     * handles displaying a message in chat
     * @param msg contains the contents of the message
     */
    public void displayMessage(String msg){
        String[] parts = msg.split(":",2);
        TextFlow message = new TextFlow();
        Text nickname = new Text("(" + parts[0] + ") ");
        Text contents = new Text(parts[1]);
        message.getChildren().addAll(nickname,contents);
        message.getStyleClass().add("messages");
        messages.getChildren().add(message);
        Multimedia.playAudio("message.wav");
        logger.info("Displaying Message");
       }

    /**
     * receives mesages form chat and sends messages to the server
     * @param msg are teh contents of the message received
     */
    public void sendMessage(String msg){
        //if the user types /nick he can change his nickname
        if(msg.contains("/nick")){
            String[] parts = msg.split(" ");
            communicator.send("NICK " + parts[1]);
        }
        //if not then a normal message is sent
        else
            communicator.send("MSG " + msg);
       }

    /**
     * handles the appearance of the scene and functionality of its buttons
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        var LobbyPane = new BorderPane();
        LobbyPane.setMaxWidth(gameWindow.getWidth());
        LobbyPane.setMaxHeight(gameWindow.getHeight());
        LobbyPane.getStyleClass().add("lobby-background");
        root.getChildren().add(LobbyPane);

        //label that displays the title
        var lobbyLabel = new Text("LOBBY");
        lobbyLabel.getStyleClass().add("bigtitle");
        lobbyLabel.setTextAlignment(TextAlignment.CENTER);
        BorderPane.setAlignment(lobbyLabel,Pos.TOP_CENTER);
        LobbyPane.setTop(lobbyLabel);

        //grid that holds contents of the scene
        var lobbyGrid = new GridPane();
        LobbyPane.setCenter(lobbyGrid);
        lobbyGrid.setVgap(5);
        lobbyGrid.setHgap(20);

        //label for the current channels
        var currentGamesLabel = new Text("Current Games");
        currentGamesLabel.getStyleClass().add("heading");
        lobbyGrid.add(currentGamesLabel,0,0);

        //HBox that holds the channels
        channelsBox = new HBox();
        channelsBox.setSpacing(10);
        lobbyGrid.add(channelsBox,1,0);

        //VBox that contains the contains of the left part
        VBox leftPart = new VBox();
        leftPart.setSpacing(5);
        lobbyGrid.add(leftPart,0,1);

        //field that prompts the user a channel to create or join
        channelField = new TextField();
        channelField.setPrefWidth((this.gameWindow.getWidth() / 4));
        channelField.setPromptText("Enter Game Name");
        leftPart.getChildren().add(channelField);

        //button to create a channel
        Button submitChannel = new Button("Create Game");
        leftPart.getChildren().add(submitChannel);

        //button to join a channel
        Button joinChannel = new Button("Join Game");
        leftPart.getChildren().add(joinChannel);

        //BorderPane that holds the objects of the chat
        var gameChat = new BorderPane();
        lobbyGrid.add(gameChat,1,1);
        gameChat.setPrefWidth(gameWindow.getWidth()/1.4);
        gameChat.setPrefHeight(gameWindow.getHeight()/1.5);
        gameChat.setMaxHeight(gameWindow.getHeight()/1.5);
        gameChat.getStyleClass().add("gameBox");
        gameChat.setVisible(false);

        //VBox that the messages are displayed in
        messages = new VBox();
        gameChat.setTop(messages);

        //HBox that displays the players in the channel
        playersList = new HBox();
        playersList.setSpacing(5);
        playersList.getStyleClass().add("playerBox");
        messages.getChildren().add(playersList);

        var bottomPart = new VBox();
        bottomPart.setSpacing(5);
        gameChat.setBottom(bottomPart);

        //HBox that holds the textfield of the chat
        HBox textBox = new HBox();
        bottomPart.getChildren().add(textBox);
        text = new TextField();
        text.setPromptText("Enter message - press Enter to send");
        textBox.getChildren().add(text);
        HBox.setHgrow(text, Priority.ALWAYS);

        //holds the buttons of the channel
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(400);
        bottomPart.getChildren().add(buttonBox);

        //button that starts the game
        startGame = new Button("Start game");
        buttonBox.getChildren().add(startGame);
        startGame.setAlignment(Pos.BOTTOM_LEFT);
        //button that leaves the channel
        var leaveGame = new Button("Leave game");
        buttonBox.getChildren().add(leaveGame);
        leaveGame.setAlignment(Pos.BOTTOM_RIGHT);

        //when this button is pressed a channel is created
        submitChannel.setOnAction(e -> {
            if(channelField.getText().isEmpty()) {
                communicator.send("ERROR");
                return;
            }
            createChannel();
            gameChat.setVisible(true);
            channelField.setVisible(false);
            submitChannel.setVisible(false);
            joinChannel.setVisible(false);
            channelField.clear();
            host.set(true);
        });

        //when this button is pressed the user joins a channel
        joinChannel.setOnAction(e -> {
            if(!channelList.contains(channelField.getText())) {
                communicator.send("ERROR");
                return;
            }
            joinChannel();
            gameChat.setVisible(true);
            channelField.setVisible(false);
            joinChannel.setVisible(false);
            submitChannel.setVisible(false);
            channelField.clear();
            host.set(false);
        });

        //when this button is pressed the user leaves the game
        leaveGame.setOnAction(e -> {
            communicator.send("PART");
            communicator.send("LIST");
            logger.info("Left Channel");
            gameChat.setVisible(false);
            channelField.setVisible(true);
            submitChannel.setVisible(true);
            joinChannel.setVisible(true);
        });

        //when this button is pressed the game starts
            startGame.setOnAction(e -> {
                communicator.send("START");
            });

            //when enter is pressed on the chat it sends the message typed in the textfield
        text.setOnKeyPressed((e) -> {
            if (e.getCode() != KeyCode.ENTER) return;
            sendMessage(text.getText());
            text.clear();
        });

    }

    /**
     * when the user escapes the scene the server stops sending messages
     * to the user
     * @param key holds the key that is pressed
     */
    public void pressedKey(KeyEvent key){
        if(key.getCode().equals(KeyCode.ESCAPE)){
            communicator.send("QUIT");
            communicator.send("PART");
            timer.purge();
            timer.cancel();
            gameWindow.startMenu();
        }
    }
}
