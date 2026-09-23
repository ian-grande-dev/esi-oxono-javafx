package be.esi.dev.oxono.model;

import be.esi.dev.oxono.command.Command;
import be.esi.dev.oxono.util.Symbol;

/**
 * Command for placing a token on the board.
 * This command can be undone and redone.
 */
public class PlaceTokenCommand implements Command {
    private final GameFacade game;
    private final Position selectedPosition;
    private final Symbol lastSymbol;

    /**
     * Creates a new place token command.
     * @param game the game where this command happens
     * @param selectedPosition where the token is placed
     * @param lastSymbol the symbol of the token that was placed
     */
    public PlaceTokenCommand(GameFacade game, Position selectedPosition, Symbol lastSymbol) {
        this.game = game;
        this.selectedPosition = selectedPosition;
        this.lastSymbol = lastSymbol;
    }

    /**
     * Undoes the token placement.
     * Removes the token and gives it back to the player.
     */
    @Override
    public void undo() {
        game.backToPlaceTokenPhase(lastSymbol, selectedPosition);
    }

    /**
     * Redoes the token placement.
     * Places the token on the board again.
     */
    @Override
    public void redo() {
        game.handlePlaceToken(selectedPosition);
    }
}
