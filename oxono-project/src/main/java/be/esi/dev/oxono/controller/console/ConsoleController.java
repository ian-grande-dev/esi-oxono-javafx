package be.esi.dev.oxono.controller.console;

import be.esi.dev.oxono.model.GameFacade;
import be.esi.dev.oxono.model.Player;
import be.esi.dev.oxono.model.Position;
import be.esi.dev.oxono.setup.GameSetupFacade;
import be.esi.dev.oxono.util.GamePhase;
import be.esi.dev.oxono.view.console.ConsoleView;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controls the game when played in the console.
 * This class handles user input and shows game information in text form.
 */
public class ConsoleController {
    private GameFacade game;
    private final GameSetupFacade gameSetup;
    private final ConsoleView view;
    private final Scanner scanner;

    /**
     * Creates a new console controller.
     */
    public ConsoleController() {
        this.game = null;
        this.gameSetup = new GameSetupFacade();
        this.view = new ConsoleView();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the game in console mode.
     */
    public void run() {
        configureGame();

        view.setGameFacade(game);
        game.registerObserver(view);
        view.update();

        startGameLoop();
    }

    /**
     * Asks the player to set up the game.
     */
    private void configureGame() {
        view.displayMessage("Welcome to OXONO!");
        view.displayMessage("Please configure the game settings.");

        promptForBoardSize();
        promptForPlayersLevel();

        this.game = new GameFacade(gameSetup.createGameConfiguration());

        view.displayMessage("Game configuration complete. Starting the game...");
    }

    /**
     * Asks the player to choose the board size.
     */
    private void promptForBoardSize() {
        while (true) {
            view.displayMessage("Enter the board size (default is 6): ");
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                return; // Use default size
            }
            try {
                int boardSize = Integer.parseInt(line);
                if (boardSize < 4) {
                    view.displayMessage("Board size must be at least 4. Please try again.");
                    continue;
                }
                gameSetup.setBoardSize(boardSize);
                return;
            } catch (NumberFormatException e) {
                view.displayMessage("Invalid input. Please enter a whole number.");
            }
        }
    }

    /**
     * Asks the player to choose the level for each player.
     */
    private void promptForPlayersLevel() {
        for (int i = 1; i <= 2; i++) {
            while (true) {
                view.displayMessage("Enter the level of Player " + i + " (0 for human, 1 ou 2 for bot): ");
                String line = scanner.nextLine();
                if (line.isEmpty()) {
                    break; // Use default level
                }
                try {
                    int level = Integer.parseInt(line);
                    if (level == 0 || level == 1 || level == 2) {
                        if (i == 1) {
                            gameSetup.setLevelPinkPlayer(level);
                        } else {
                            gameSetup.setLevelBlackPlayer(level);
                        }
                        break;
                    } else {
                        view.displayMessage("Invalid level. Please enter 0 for human, 1 or 2 for bot.");
                    }
                } catch (NumberFormatException e) {
                    view.displayMessage("Invalid input. Please enter a whole number.");
                }
            }
        }
    }

    /**
     * Runs the main game loop until the game ends.
     */
    private void startGameLoop() {
        while (game.getWinner() == null && game.getGamePhase() != GamePhase.GAME_OVER) {
            Player currentPlayer = game.getCurrentPlayer();

            try {
                if (currentPlayer.isBot()) {
                    handleBotTurn();
                } else {
                    handleHumanTurn();
                }
            }
            catch (RuntimeException e) {
                view.displayMessage("An error occurred: " + e.getMessage());
            }
        }
        view.displayMessage("Game finished.");
    }

    /**
     * Handles a human player's turn.
     */
    private void handleHumanTurn() {
        boolean actionSuccessful = false;
        while (!actionSuccessful) {
            try {
                Position position = promptForPositionOrAction();
                game.handlePlayerAction(position);
                actionSuccessful = true;
            } catch (RuntimeException e) {
                view.displayMessage("Invalid action : " + e.getMessage() + ". Please try again.");
            }
        }
    }

    /**
     * Handles a bot player's turn.
     */
    private void handleBotTurn() {
        view.displayMessage("The Bot is thinking...");
        try {
            // Wait a bit so player can see the bot is thinking
            Thread.sleep(750);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            Position chosenPosition = game.handleBotAction();
            view.displayMessage("The AI plays in " + chosenPosition);
        }
        catch (Exception e) {
            view.displayMessage("The Bot encountered an error: " + e.getMessage());
        }
    }

    /**
     * Asks the player for their next move or action.
     * @return the position chosen by the player, or null for special actions
     */
    private Position promptForPositionOrAction() {
        while (true) {
            view.displayMessage("Enter coordinates (x y) or an action (R, UNDO, REDO, SURRENDER): ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "R":
                    // Let the bot choose for this player
                    return game.getCurrentPlayer().getStrategy().chooseAction(game);
                case "UNDO":
                    game.undo();
                    return null;
                case "REDO":
                    game.redo();
                    return null;
                case "SURRENDER":
                    game.surrender();
                    return null;
                default:
                    try {
                        return regexToPosition(input);
                    } catch (Exception e) {
                        view.displayMessage("Invalid input. Please enter coordinates in the format 'x y'.");
                    }
            }
        }
    }

    /**
     * Converts text input to a position on the board.
     * @param positionString the text input from the player
     * @return the position on the board
     */
    private Position regexToPosition(String positionString) {
        Pattern pattern = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s*$");
        Matcher matcher = pattern.matcher(positionString);
        if (matcher.matches()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                return new Position(x, y);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid input format for position: " + positionString, e);
            }
        } else {
            throw new IllegalArgumentException("Invalid input format for position: " + positionString);
        }
    }
}
