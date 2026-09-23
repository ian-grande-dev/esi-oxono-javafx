package be.esi.dev.oxono.strategy;

import be.esi.dev.oxono.model.*;
import be.esi.dev.oxono.util.GamePhase;

import java.util.List;
import java.util.Random;

/**
 * A smart strategy that tries to find winning moves.
 * This bot looks ahead to see if it can win in the next few moves.
 */
public class WiningMoveStrategy implements PlayerStrategy {

    // Positions chosen for each phase of the move
    private Position chooseTotemPosition;
    private Position moveTotemPosition;
    private Position placeTokenPosition;

    /**
     * Chooses the best move by looking for winning combinations.
     * @param game the current game state
     * @return the best position to play, or random if no winning move found
     */
    @Override
    public Position chooseAction(GameFacade game) {
        Position resultPosition = null;

        // Choose action based on current game phase
        switch (game.getGamePhase()) {
            case CHOOSE_TOTEM -> {
                analyseTheGame(game); // Look for winning sequence starting with totem choice
                resultPosition = chooseTotemPosition;
            }
            case MOVE_TOTEM -> {
                analyseMovePosition(game); // Look for winning move with current totem
                resultPosition = moveTotemPosition;
            }
            case PLACE_TOKEN -> {
                analysePlacePosition(game); // Look for winning token placement
                resultPosition = placeTokenPosition;
            }
            default -> resultPosition = StrategyUtils.randomPosition(game.getValidPositions());
        }
        // If no winning move found, play randomly
        return resultPosition != null ? resultPosition : StrategyUtils.randomPosition(game.getValidPositions());
    }

    /**
     * Looks for a complete winning sequence starting from totem choice.
     * Tests all possible totem choices to see if any leads to victory.
     * @param game the current game state
     */
    private void analyseTheGame(GameFacade game) {
        // Try each possible totem choice
        for (Position chooseTotemPos : game.getValidPositions()) {
            chooseTotemPosition = chooseTotemPos;
            // See if this totem choice can lead to a win
            if (analyseMovePosition(createGameCopy(game, chooseTotemPos))) {
                return; // Found winning sequence
            }
        }
        resetPositions(); // No winning sequence found
    }

    /**
     * Looks for a winning move sequence starting from totem movement.
     * @param game the current game state
     * @return true if a winning sequence is found
     */
    private boolean analyseMovePosition(GameFacade game) {
        // Try each possible totem move
        for (Position moveTotemPos : game.getValidPositions()) {
            moveTotemPosition = moveTotemPos;
            // See if this move can lead to a win
            if (analysePlacePosition(createGameCopy(game, moveTotemPos))) {
                return true; // Found winning sequence
            }
        }
        resetPositions(); // No winning sequence found
        return false;
    }

    /**
     * Looks for a winning token placement.
     * @param game the current game state
     * @return true if a winning move is found
     */
    private boolean analysePlacePosition(GameFacade game) {
        // Try each possible token placement
        for (Position tokenPos : game.getValidPositions()) {
            // Check if this placement wins the game
            if (isGameOver(createGameCopy(game, tokenPos))) {
                placeTokenPosition = tokenPos;
                return true; // Found winning move
            }
        }
        return false; // No winning move found
    }

    /**
     * Clears all saved positions when no winning sequence is found.
     */
    private void resetPositions() {
        chooseTotemPosition = null;
        moveTotemPosition = null;
        placeTokenPosition = null;
    }

    /**
     * Creates a copy of the game and plays one action on it.
     * Used to test moves without changing the real game.
     * @param game the original game
     * @param action the move to test
     * @return a copy of the game with the move played
     */
    private GameFacade createGameCopy(GameFacade game, Position action) {
        GameFacade gameCopy = new GameFacade(game);
        gameCopy.handlePlayerAction(action);
        return gameCopy;
    }

    /**
     * Checks if the game has ended.
     * @param game the game to check
     * @return true if the game is over
     */
    private boolean isGameOver(GameFacade game) {
        return game.getGamePhase() == GamePhase.GAME_OVER;
    }
}