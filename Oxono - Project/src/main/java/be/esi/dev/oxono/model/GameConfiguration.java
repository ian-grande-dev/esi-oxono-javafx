package be.esi.dev.oxono.model;

/**
 * Holds the settings for starting a new game.
 * This record contains all the information needed to create a game.
 * @param pinkPlayer the pink player (human or bot)
 * @param blackPlayer the black player (human or bot)
 * @param boardSize how big the game board should be
 */
public record GameConfiguration(Player pinkPlayer, Player blackPlayer, int boardSize) {
}
