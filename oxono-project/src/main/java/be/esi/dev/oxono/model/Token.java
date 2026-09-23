package be.esi.dev.oxono.model;

import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.Symbol;

public class Token extends Pawn {

    private final Color color;

    public Token(Symbol symbol, Color color) {
        setSymbol(symbol);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
