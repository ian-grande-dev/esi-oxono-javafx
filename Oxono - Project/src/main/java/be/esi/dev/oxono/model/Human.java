package be.esi.dev.oxono.model;

import be.esi.dev.oxono.strategy.RandomStrategy;
import be.esi.dev.oxono.util.Color;

/**
 * A human player that makes moves by clicking or typing.
 * This player waits for input from a real person.
 */
public class Human extends Player{

    /**
     * Creates a new human player.
     * @param color the color of this player (pink or black)
     * @param tokensPerSymbol how many tokens of each symbol this player has
     */
    public Human(Color color, int tokensPerSymbol) {
        setColor(color);
        setRemainingTokensO(tokensPerSymbol);
        setRemainingTokensX(tokensPerSymbol);
        setBot(false);
        // Humans have a random strategy for the "R" command
        setStrategy(new RandomStrategy());
    }
}
