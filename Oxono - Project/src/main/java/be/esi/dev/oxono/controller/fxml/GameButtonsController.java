package be.esi.dev.oxono.controller.fxml;

import be.esi.dev.oxono.model.GameFacade;
import be.esi.dev.oxono.util.GamePhase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * Controls the game buttons like undo, redo, and surrender.
 * This class handles button clicks and shows messages to the player.
 */
public class GameButtonsController {

    private GameFacade gameFacade;

    @FXML
    private Label turnMessageLabel;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private Label errorMessageLabel;

    // Timer for surrender button
    private Timeline surrenderTimer;
    private boolean surrenderInProgress = false;

    /**
     * Handles click on the undo button.
     * Takes back the last move if possible.
     */
    @FXML
    private void onUndoButtonClick() {
        errorMessageLabel.setText("");
        try {
            if (gameFacade.getGamePhase() != GamePhase.GAME_OVER) {
                gameFacade.undo();
                statusMessageLabel.setText("The last action has been undone.");
            }
            else {
                statusMessageLabel.setText("The game is over. No more actions can be undone.");
            }
        }
        catch (Exception e) {
            errorMessageLabel.setText(e.getMessage());
        }
    }

    /**
     * Handles click on the redo button.
     * Puts back the last undone move if possible.
     */
    @FXML
    private void onRedoButtonClick() {
        errorMessageLabel.setText("");
        try {
            if (gameFacade.getGamePhase() != GamePhase.GAME_OVER) {
                gameFacade.redo();
                statusMessageLabel.setText("The last action has been redone.");
            }
            else {
                statusMessageLabel.setText("The game is over. No more actions can be redone.");
            }
        }
        catch (Exception e) {
            errorMessageLabel.setText(e.getMessage());
        }
    }

    /**
     * Handles when the surrender button is pressed down.
     * Starts a timer - player must hold for 5 seconds to surrender.
     * @param event the mouse press event
     */
    @FXML
    private void onSurrenderButtonPressed(MouseEvent event) {
        if (gameFacade.getGamePhase() == GamePhase.GAME_OVER) {
            statusMessageLabel.setText("The game is already over.");
            return;
        }

        surrenderInProgress = true;
        statusMessageLabel.setText("Hold button for 5 seconds to surrender...");

        // Start timer for 5 seconds
        surrenderTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            if (surrenderInProgress) {
                executeSurrender();
            }
        }));
        surrenderTimer.play();
    }

    /**
     * Handles when the surrender button is released.
     * Cancels the surrender if player lets go too early.
     * @param event the mouse release event
     */
    @FXML
    private void onSurrenderButtonReleased(MouseEvent event) {
        if (surrenderTimer != null) {
            surrenderTimer.stop();
        }
        surrenderInProgress = false;
        if (gameFacade.getGamePhase() != GamePhase.GAME_OVER) {
            statusMessageLabel.setText("");
        }
    }

    /**
     * Actually surrenders the game.
     * Called when player holds surrender button for 5 seconds.
     */
    private void executeSurrender() {
        errorMessageLabel.setText("");
        try {
            gameFacade.surrender();
            statusMessageLabel.setText("The current player has surrendered.");
        } catch (Exception e) {
            errorMessageLabel.setText(e.getMessage());
        }
    }

    /**
     * Handles click on the back to setup button.
     * Returns to the game setup screen.
     */
    @FXML
    private void onBackToSetupButtonClick() {
        errorMessageLabel.setText("");
        try {
            // Load the setup screen
            URL resource = SetupController.class.getResource("/view/setup.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = (Stage) turnMessageLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            errorMessageLabel.setText("Error loading game view: " + e.getMessage());
        }
    }

    /**
     * Sets the game logic for this controller.
     * @param gameFacade the game logic to use
     */
    public void setGameFacade(GameFacade gameFacade) {
        this.gameFacade = gameFacade;
    }

    /**
     * Sets the labels used to show messages to the player.
     * @param turnLabel shows whose turn it is
     * @param statusLabel shows game status
     * @param errorLabel shows error messages
     */
    public void setMessageLabel(Label turnLabel, Label statusLabel, Label errorLabel) {
        this.turnMessageLabel = turnLabel;
        this.statusMessageLabel = statusLabel;
        this.errorMessageLabel = errorLabel;
    }
}
