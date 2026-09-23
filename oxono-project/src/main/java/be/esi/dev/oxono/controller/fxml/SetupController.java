package be.esi.dev.oxono.controller.fxml;

import be.esi.dev.oxono.model.GameConfiguration;
import be.esi.dev.oxono.model.GameFacade;
import be.esi.dev.oxono.model.Human;
import be.esi.dev.oxono.model.Player;
import be.esi.dev.oxono.setup.GameSetupFacade;
import be.esi.dev.oxono.util.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Controls the game setup screen.
 * This class lets players choose game settings before starting.
 */
public class SetupController {

    private static final int MIN_BOARD_SIZE = 4;
    private static final int MAX_BOARD_SIZE = 10;

    @FXML
    private ComboBox<String> player1LevelComboBox;

    @FXML
    private ComboBox<String> player2LevelComboBox;

    @FXML
    private TextField boardSizeField;

    @FXML
    private Button startButton;

    @FXML
    private Label statusMessageLabel;

    private GameSetupFacade gameSetupFacade;

    /**
     * Sets up the controller when the window loads.
     */
    @FXML
    private void initialize() {
        // Only allow numbers in the board size field
        boardSizeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                boardSizeField.setText(oldValue);
            }
        });
        gameSetupFacade = new GameSetupFacade();
    }

    /**
     * Handles click on the start game button.
     * Reads all settings and starts the game.
     * @param event the button click event
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        String player1Level = player1LevelComboBox.getValue();
        String player2Level = player2LevelComboBox.getValue();

        // Check if board size is valid
        int boardSize = parseAndValidateBoardSize();
        if (boardSize == -1) {
            statusMessageLabel.setText("Invalid board size. Please enter a number between "
                    + MIN_BOARD_SIZE + " and " + MAX_BOARD_SIZE + ".");
            return;
        }

        // Set up the game with chosen settings
        configureGame(player1Level, player2Level, boardSize);
        GameConfiguration gameConfiguration = gameSetupFacade.createGameConfiguration();
        loadGameView(gameConfiguration);
    }

    /**
     * Sets up the game with the chosen settings.
     * @param player1Level what type of player 1 is
     * @param player2Level what type of player 2 is
     * @param boardSize how big the game board should be
     */
    private void configureGame(String player1Level, String player2Level, int boardSize) {
        gameSetupFacade.setBoardSize(boardSize);
        configurePlayer1(player1Level);
        configurePlayer2(player2Level);
    }

    /**
     * Sets up player 1 based on the chosen level.
     * @param playerLevel the type of player (human or bot)
     */
    private void configurePlayer1(String playerLevel) {
        switch (playerLevel) {
            case "Human":
                gameSetupFacade.setLevelPinkPlayer(0);
                break;
            case "Bot (easy)":
                gameSetupFacade.setLevelPinkPlayer(1);
                break;
            case "Bot (medium)":
                gameSetupFacade.setLevelPinkPlayer(2);
                break;
        }
    }

    /**
     * Sets up player 2 based on the chosen level.
     * @param playerLevel the type of player (human or bot)
     */
    private void configurePlayer2(String playerLevel) {
        switch (playerLevel) {
            case "Human":
                gameSetupFacade.setLevelBlackPlayer(0);
                break;
            case "Bot (easy)":
                gameSetupFacade.setLevelBlackPlayer(1);
                break;
            case "Bot (medium)":
                gameSetupFacade.setLevelBlackPlayer(2);
                break;
        }
    }

    /**
     * Loads the game screen with the chosen settings.
     * @param gameConfiguration the game settings to use
     */
    private void loadGameView(GameConfiguration gameConfiguration) {
        try {
            // Load the game screen
            URL resource = SetupController.class.getResource("/view/game.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            // Give the settings to the game controller
            GameController gameController = loader.getController();
            gameController.setGameConfiguration(gameConfiguration);

            // Show the game screen
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            statusMessageLabel.setText("Error loading game view: " + e.getMessage());
        }
    }

    /**
     * Checks if the board size is valid.
     * @return the board size if valid, -1 if invalid
     */
    private int parseAndValidateBoardSize() {
        try {
            int size = Integer.parseInt(boardSizeField.getText());
            if (size >= MIN_BOARD_SIZE && size <= MAX_BOARD_SIZE) {
                return size;
            }
        } catch (NumberFormatException e) {
            // Invalid input: handled by the caller
        }
        return -1;
    }
}
