package be.esi.dev.oxono.controller.fxml;

import be.esi.dev.oxono.model.GameConfiguration;
import be.esi.dev.oxono.model.GameFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Main controller for the game window.
 * This class connects all the different parts of the game interface.
 */
public class GameController {

    @FXML
    private BoardController boardController;

    @FXML
    private GameButtonsController gameButtonsController;

    @FXML
    private PlayerInformationController playerInformationController;

    @FXML
    private Label turnMessageLabel;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private Label errorMessageLabel;

    private GameFacade gameFacade;

    /**
     * Sets up the controller when the window loads.
     */
    @FXML
    private void initialize() {
        setMessageLabel(turnMessageLabel, statusMessageLabel, errorMessageLabel);
    }

    /**
     * Sets up the game with the chosen settings.
     * @param gameConfiguration the game settings to use
     */
    public void setGameConfiguration(GameConfiguration gameConfiguration) {
        this.gameFacade = new GameFacade(gameConfiguration);

        // Give the game logic to all sub-controllers
        if (boardController != null) {
            boardController.setGameFacade(gameFacade);
        }
        if (gameButtonsController != null) {
            gameButtonsController.setGameFacade(gameFacade);
        }
        if (playerInformationController != null) {
            playerInformationController.setGameFacade(gameFacade);
        }
    }

    /**
     * Sets the labels used to show messages to the player.
     * @param turnlabel shows whose turn it is
     * @param statusLabel shows game status
     * @param errorLabel shows error messages
     */
    public void setMessageLabel(Label turnlabel, Label statusLabel, Label errorLabel) {
        if (boardController != null) {
            boardController.setMessageLabel(turnlabel, statusLabel, errorLabel);
        }
        if (gameButtonsController != null) {
            gameButtonsController.setMessageLabel(turnlabel, statusLabel, errorLabel);
        }
    }
}