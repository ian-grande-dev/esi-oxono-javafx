package be.esi.dev.oxono.controller.fxml;

import be.esi.dev.oxono.model.GameFacade;
import be.esi.dev.oxono.observer.Observer;
import be.esi.dev.oxono.util.Symbol;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Shows information about the players.
 * This class displays how many game pieces each player has left.
 */
public class PlayerInformationController implements Observer {
    @FXML
    private Label player1XTokensLabel;

    @FXML
    private Label player1OTokensLabel;

    @FXML
    private Label player2XTokensLabel;

    @FXML
    private Label player2OTokensLabel;

    private GameFacade gameFacade;

    /**
     * Updates the player information when the game changes.
     * Shows how many pieces each player has left.
     */
    @Override
    public void update() {
        // Update Player 1 (Pink) token counts
        int remainingTokensPlayer1X = gameFacade.getPinkPlayer().getRemainingTokens(Symbol.X);
        player1XTokensLabel.setText(String.valueOf(remainingTokensPlayer1X));

        int remainingTokensPlayer1O = gameFacade.getPinkPlayer().getRemainingTokens(Symbol.O);
        player1OTokensLabel.setText(String.valueOf(remainingTokensPlayer1O));

        // Update Player 2 (Black) token counts
        int remainingTokensPlayer2X = gameFacade.getBlackPlayer().getRemainingTokens(Symbol.X);
        player2XTokensLabel.setText(String.valueOf(remainingTokensPlayer2X));

        int remainingTokensPlayer2O = gameFacade.getBlackPlayer().getRemainingTokens(Symbol.O);
        player2OTokensLabel.setText(String.valueOf(remainingTokensPlayer2O));
    }

    /**
     * Sets the game logic for this controller.
     * @param gameFacade the game logic to use
     */
    public void setGameFacade(GameFacade gameFacade) {
        this.gameFacade = gameFacade;
        this.gameFacade.registerObserver(this);
        update();
    }
}
