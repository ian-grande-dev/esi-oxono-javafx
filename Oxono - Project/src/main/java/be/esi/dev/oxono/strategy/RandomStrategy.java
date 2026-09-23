package be.esi.dev.oxono.strategy;

import be.esi.dev.oxono.model.GameFacade;
import be.esi.dev.oxono.model.Position;

import java.util.List;
import java.util.Random;

/**
 * A simple strategy that chooses moves randomly.
 * This bot picks any valid move without thinking about winning.
 */
public class RandomStrategy implements PlayerStrategy {

    /**
     * Chooses a random valid move from the available options.
     * @param game the current game state
     * @return a random position from the valid moves
     */
    @Override
    public Position chooseAction(GameFacade game) {
        return StrategyUtils.randomPosition(game.getValidPositions());
    }
}
