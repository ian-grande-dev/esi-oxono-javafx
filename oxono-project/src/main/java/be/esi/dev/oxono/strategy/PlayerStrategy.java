package be.esi.dev.oxono.strategy;

import be.esi.dev.oxono.model.GameFacade;
import be.esi.dev.oxono.model.Position;

public interface PlayerStrategy {
    Position chooseAction(GameFacade game);
}
