package be.esi.dev.oxono.strategy;

import be.esi.dev.oxono.model.Position;

import java.util.List;
import java.util.Random;

/**
 * Utility class for game strategies.
 */
public final class StrategyUtils {

    private static final Random RANDOM = new Random();

    // Prevents instantiation
    private StrategyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Selects a random position from a list of valid positions.
     * @param positions the list of possible positions
     * @return a random position, or null if the list is empty
     */
    static Position randomPosition(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            return null;
        }
        return positions.get(RANDOM.nextInt(positions.size()));
    }
}