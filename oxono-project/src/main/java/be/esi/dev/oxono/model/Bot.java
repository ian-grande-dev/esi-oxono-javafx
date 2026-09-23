package be.esi.dev.oxono.model;

import be.esi.dev.oxono.strategy.PlayerStrategy;
import be.esi.dev.oxono.util.Color;

/**
 * A computer player that plays automatically.
 * This player uses a strategy to choose moves without human input.
 */
public class Bot extends Player {

    /**
     * Creates a new bot player.
     * @param color the color of this bot (pink or black)
     * @param tokensPerSymbol how many tokens of each symbol this bot has
     * @param strategy the strategy this bot uses to choose moves
     */
    public Bot(Color color, int tokensPerSymbol, PlayerStrategy strategy) {
        setColor(color);
        setRemainingTokensO(tokensPerSymbol);
        setRemainingTokensX(tokensPerSymbol);
        setBot(true);
        setStrategy(strategy);
    }
}
