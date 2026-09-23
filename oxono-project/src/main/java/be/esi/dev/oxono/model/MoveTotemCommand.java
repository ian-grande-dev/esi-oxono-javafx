package be.esi.dev.oxono.model;

import be.esi.dev.oxono.command.Command;

/**
 * Command for moving a totem to a new position.
 * This command can be undone and redone.
 */
public class MoveTotemCommand implements Command {
    private final GameFacade game;
    private final Position selectedPosition;
    private final Position previousTotemPosition;

    /**
     * Creates a new move totem command.
     * @param game the game where this command happens
     * @param selectedPosition where the totem is moved to
     * @param previousTotemPosition where the totem was before moving
     */
    public MoveTotemCommand(GameFacade game, Position selectedPosition, Position previousTotemPosition) {
        this.game = game;
        this.selectedPosition = selectedPosition;
        this.previousTotemPosition = previousTotemPosition;
    }

    /**
     * Undoes the totem move.
     * Moves the totem back to its previous position.
     */
    @Override
    public void undo() {
        game.backToMoveTotemPhase(previousTotemPosition);
    }

    /**
     * Redoes the totem move.
     * Moves the totem to the new position again.
     */
    @Override
    public void redo() {
        game.handleMoveTotem(selectedPosition);
    }
}
