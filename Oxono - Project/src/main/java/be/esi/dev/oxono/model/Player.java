package be.esi.dev.oxono.model;

import be.esi.dev.oxono.strategy.PlayerStrategy;
import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.Symbol;

/**
 * Base class for all players in the game.
 * A player can be human or bot, and has tokens to place on the board.
 */
public abstract class Player {
    private Color color;
    private int remainingTokensO;
    private int remainingTokensX;
    private boolean isBot;
    private PlayerStrategy strategy;

    /**
     * Gets the color of this player.
     * @return the player's color (pink or black)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets how many tokens of a symbol this player has left.
     * @param symbol the symbol to check (O or X)
     * @return the number of remaining tokens
     */
    public int getRemainingTokens(Symbol symbol) {
        if (symbol == Symbol.O) {
            return remainingTokensO;
        } else if (symbol == Symbol.X) {
            return remainingTokensX;
        } else {
            throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }
    }

    /**
     * Gets the strategy this player uses to choose moves.
     * @return the player's strategy
     */
    public PlayerStrategy getStrategy() {
        return strategy;
    }

    /**
     * Checks if this player is controlled by the computer.
     * @return true if this is a bot, false if human
     */
    public boolean isBot() {
        return isBot;
    }

    /**
     * Checks if this player has no tokens left to play.
     * @return true if player cannot make any more moves
     */
    public boolean hasNoTokensLeft() {
        if (remainingTokensO <= 0 && remainingTokensX <= 0) {
            return true;
        } else if (remainingTokensO < 0 || remainingTokensX < 0) {
            throw new IllegalStateException("Remaining tokens cannot be negative.");
        }
        return false;
    }

    /**
     * Gives a token back to the player (used for undo).
     * @param symbol the symbol of the token to give back
     */
    protected void giveBackToken(Symbol symbol) {
        if (symbol == Symbol.O) {
            remainingTokensO++;
        }
        else {
            remainingTokensX++;
        }
    }

    /**
     * Takes a token from the player and creates it for placement.
     * @param symbol the symbol of the token to use
     * @return a new token ready to be placed on the board
     * @throws IllegalArgumentException if player has no tokens of that symbol
     */
    protected Token consumeToken(Symbol symbol) {
        if (symbol == Symbol.O && remainingTokensO > 0) {
            remainingTokensO--;
            return new Token(symbol, color);
        } else if (symbol == Symbol.X && remainingTokensX > 0) {
            remainingTokensX--;
            return new Token(symbol, color);
        } else {
            throw new IllegalArgumentException("No tokens of type " + symbol + " available.");
        }
    }

    /**
     * Sets the color for this player.
     * @param color the color to set (pink or black)
     */
    protected void setColor(Color color) {
        this.color = color;
    }

    /**
     * Sets how many O tokens this player has.
     * @param remainingTokensO the number of O tokens
     */
    protected void setRemainingTokensO(int remainingTokensO) {
        this.remainingTokensO = remainingTokensO;
    }

    /**
     * Sets how many X tokens this player has.
     * @param remainingTokensX the number of X tokens
     */
    protected void setRemainingTokensX(int remainingTokensX) {
        this.remainingTokensX = remainingTokensX;
    }

    /**
     * Sets whether this player is controlled by computer.
     * @param isBot true for bot, false for human
     */
    protected void setBot(boolean isBot) {
        this.isBot = isBot;
    }

    /**
     * Sets the strategy this player uses to choose moves.
     * @param strategy the strategy to use
     */
    protected void setStrategy(PlayerStrategy strategy) {
        this.strategy = strategy;
    }
}
