package be.esi.dev.oxono.model;

import be.esi.dev.oxono.command.Command;

/**
 * Command for choosing which totem to use.
 * This command can be undone and redone.
 */
public class ChooseTotemCommand implements Command {
    private final GameFacade game;
    private final Position selectedPosition;

    /**
     * Creates a new choose totem command.
     * @param game the game where this command happens
     * @param selectedPosition the position of the chosen totem
     */
    public ChooseTotemCommand(GameFacade game, Position selectedPosition) {
        this.game = game;
        this.selectedPosition = selectedPosition;
    }

    /**
     * Undoes the totem selection.
     * Goes back to the choose totem phase.
     */
    @Override
    public void undo() {
        game.backToChooseTotemPhase();
    }

    /**
     * Redoes the totem selection.
     * Chooses the totem again.
     */
    @Override
    public void redo() {
        game.handleChooseTotem(selectedPosition);
    }
}
