package uk.ac.soton.comp1206.Utilities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    //checks whether an audio can be played or not
    private static BooleanProperty audioEnabledProperty = new SimpleBooleanProperty(true);

    //media player object that handles sound effects
    private static MediaPlayer mediaPlayer;
    //media player object that handles music
    private static MediaPlayer musicPlayer;

    //store the volume for the effects and music
    private static double musicVolume = 1;
    private static double mediaVolume = 1;

    /**
     * plays background music
     * @param file stores the file of the music to be played
     */
    public static void playMusic(String file) {
        if (!getAudioEnabled()) return;

        String music = Multimedia.class.getResource("/music/" + file).toExternalForm();
        logger.info("Playing Background Music: " + music);

        if(musicPlayer != null){
            musicPlayer.stop();
        }

        try {
            Media play = new Media(music);
            musicPlayer = new MediaPlayer(play);
            musicPlayer.setVolume(musicVolume);

            //lets the music play in the background without stopping
            musicPlayer.setOnEndOfMedia(() -> musicPlayer.seek(Duration.ZERO));
            musicPlayer.play();

        } catch (Exception e) {
            //audio is disabled
            setAudioEnabled(false);
            e.printStackTrace();
            logger.error("Unable to play music file, stopping music");
        }
    }
    /**
     * plays the music effects
     * @param file stores the file of the audio to be played
     */
    public static void playAudio(String file) {
        if (!getAudioEnabled()) return;

        String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing audio: " + toPlay);

        try {
            Media play = new Media(toPlay);
            mediaPlayer = new MediaPlayer(play);
            mediaPlayer.setVolume(mediaVolume);
            mediaPlayer.play();
        } catch (Exception e) {
            setAudioEnabled(false);
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    public static BooleanProperty audioEnabledProperty(){
        return audioEnabledProperty;
    }

    public static void setAudioEnabled(boolean enabled) {
        logger.info("Audio enabled set to: " + enabled);
        audioEnabledProperty().set(enabled);
    }

    public static boolean getAudioEnabled() {
        return audioEnabledProperty().get();
    }

    /**
     * handles placing an image
     * @param file stores the file of the image
     * @return the image to be displayed or nothing if an exception is thrown
     */
    public static Image getImage(String file) {
        try {
            return new Image(Multimedia.class.getResource("/images/" + file).toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to load image");
            return null;
        }
    }
    public static double getMusicVolume(){
        return musicPlayer.getVolume();
    }
    public static double getMediaVolume(){
        return mediaPlayer.getVolume();
    }

    /**
     * sets the music volume
     * @param musicVolume stores the volume of the music
     */
    public static void setMusicVolume(double musicVolume){
       Multimedia.musicVolume = musicVolume;
         musicPlayer.setVolume(musicVolume);
    }

    /**
     * sets the effects volume
     * @param mediaVolume stores the volume of the effects
     */
    public static void setMediaVolume(double mediaVolume){
        Multimedia.mediaVolume = mediaVolume;
        mediaPlayer.setVolume(mediaVolume);
    }
}
