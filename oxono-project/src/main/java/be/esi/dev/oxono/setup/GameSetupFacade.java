package be.esi.dev.oxono.setup;

import be.esi.dev.oxono.model.Bot;
import be.esi.dev.oxono.model.GameConfiguration;
import be.esi.dev.oxono.model.Human;
import be.esi.dev.oxono.model.Player;
import be.esi.dev.oxono.strategy.RandomStrategy;
import be.esi.dev.oxono.strategy.WiningMoveStrategy;
import be.esi.dev.oxono.util.Color;

/**
 * Helps set up a new game with chosen settings.
 * This class creates players and configures the game board.
 */
public class GameSetupFacade {
    private int levelPinkPlayer;
    private int levelBlackPlayer;
    private int boardSize;

    /**
     * Creates a new game setup with default settings.
     * Pink player is human, black player is easy bot, board size is 6.
     */
    public GameSetupFacade() {
        this.levelPinkPlayer = 0; // 0 = human player
        this.levelBlackPlayer = 1; // 1 = easy bot
        this.boardSize = 6; // 6x6 board
    }

    /**
     * Creates the final game configuration with all chosen settings.
     * @return the complete game configuration ready to start
     */
    public GameConfiguration createGameConfiguration() {
        Player pinkPlayer = createPlayer(levelPinkPlayer, Color.PINK);
        Player blackPlayer = createPlayer(levelBlackPlayer, Color.BLACK);
        return new GameConfiguration(pinkPlayer, blackPlayer, boardSize);
    }

    /**
     * Creates a player with the given level and color.
     * @param level 0 for human, 1 for easy bot, 2 for medium bot
     * @param color the color for this player (pink or black)
     * @return the created player
     * @throws IllegalArgumentException if level is not 0, 1, or 2
     */
    private Player createPlayer(int level, Color color) {
        // Calculate how many tokens each player gets
        int tokensPerSymbol =  (boardSize * boardSize - 4) / 4;

        if (level == 0) {
            // Human player
            return new Human(color, tokensPerSymbol);
        }
        else if (level == 1) {
            // Easy bot - plays randomly
            return new Bot(color, tokensPerSymbol, new RandomStrategy());
        }
        else if (level == 2) {
            // Medium bot - tries to win
            return new Bot(color, tokensPerSymbol, new WiningMoveStrategy());
        }
        else {
            throw new IllegalArgumentException("Invalid player level: " + level);
        }
    }

    /**
     * Sets the level for the pink player.
     * @param levelPinkPlayer 0 for human, 1 for easy bot, 2 for medium bot
     */
    public void setLevelPinkPlayer(int levelPinkPlayer) {
        this.levelPinkPlayer = levelPinkPlayer;
    }

    /**
     * Sets the level for the black player.
     * @param levelBlackPlayer 0 for human, 1 for easy bot, 2 for medium bot
     */
    public void setLevelBlackPlayer(int levelBlackPlayer) {
        this.levelBlackPlayer = levelBlackPlayer;
    }

    /**
     * Sets the size of the game board.
     * @param boardSize the number of rows and columns for the board
     */
    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}
