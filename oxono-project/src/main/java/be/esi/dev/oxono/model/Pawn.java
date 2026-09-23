package be.esi.dev.oxono.model;

import be.esi.dev.oxono.util.Symbol;

/**
 * Base class for all game pieces.
 * Every piece on the board is a pawn with a symbol (O or X).
 */
public abstract class Pawn {
    private Symbol symbol;

    /**
     * Sets the symbol for this game piece.
     * @param symbol the symbol (O or X) for this piece
     */
    protected void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the symbol of this game piece.
     * @return the symbol (O or X) of this piece
     */
    public Symbol getSymbol() {
        return symbol;
    }
}
