package be.esi.dev.oxono.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Starting point for the graphical version of the game.
 * Run this class to play the game with buttons and images.
 */
public class MainFX extends Application {

    /**
     * Sets up and shows the game window.
     * This method runs when the application starts.
     * @param primaryStage the main window of the application
     * @throws Exception if there's an error loading the window
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the setup screen from FXML file
        URL resource = MainFX.class.getResource("/view/setup.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        // Show the window
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Starts the graphical version of the OXONO game.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(); // Start the JavaFX application
    }
}
