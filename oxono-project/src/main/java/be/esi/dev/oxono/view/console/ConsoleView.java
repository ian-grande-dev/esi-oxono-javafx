package be.esi.dev.oxono.view.console;

import be.esi.dev.oxono.model.*;
import be.esi.dev.oxono.observer.Observer;
import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.GamePhase;
import be.esi.dev.oxono.util.Symbol;

import java.util.List;
import java.util.Map;

/**
 * Shows the game in the console with text and colors.
 * This class displays the board, players, and game information using text.
 */
public class ConsoleView implements Observer {
    private GameFacade game;

    // Color codes for making text colorful in console
    private String RESET = "\u001B[0m";
    private String PINK  = "\u001B[95m";
    private String BLUE  = "\u001B[94m";
    private String GRAY  = "\u001B[90m";

    /**
     * Shows the game board in the console.
     * @param boardState all the pieces on the board
     * @param size how big the board is
     * @param validPositions positions where the player can move
     */
    public void displayBoard(Map<Position, Pawn> boardState, int size, List<Position> validPositions) {
        String space = "\t";

        // Draw each row of the board
        for (int i = 0; i < size; i++) {
            System.out.print(space + i + " |");
            for (int j = 0; j < size; j++) {
                Position currentPos = new Position(j, i);
                Pawn pawn = boardState.get(currentPos);
                displayCell(currentPos, pawn, validPositions, space);
            }
            System.out.println();
        }
    }

    /**
     * Shows one cell of the board.
     * @param position where this cell is
     * @param pawn the piece in this cell (can be null)
     * @param validPositions positions where player can move
     * @param space spacing for display
     */
    private void displayCell(Position position, Pawn pawn, List<Position> validPositions, String space) {
        if (pawn == null) {
            displayEmptyCell(position, validPositions, space);
        } else if (pawn instanceof Token token) {
            displayToken(token, space);
        } else if (pawn instanceof Totem totem) {
            displayTotem(totem, space);
        }
    }

    /**
     * Shows an empty cell on the board.
     * Highlights it if the player can move there.
     * @param position where this cell is
     * @param validPositions positions where player can move
     * @param space spacing for display
     */
    private void displayEmptyCell(Position position, List<Position> validPositions, String space) {
        if (validPositions != null && validPositions.contains(position)) {
            // Show blue dot for valid moves
            System.out.print(space + BLUE + ". " + RESET);
        } else {
            // Show normal dot for empty cells
            System.out.print(space + RESET + ". " + RESET);
        }
    }

    /**
     * Shows a token (player piece) on the board.
     * @param token the token to show
     * @param space spacing for display
     */
    private void displayToken(Token token, String space) {
        String symbol = token.getSymbol() == Symbol.O ? "O" : "X";
        String color = token.getColor() == Color.PINK ? PINK : GRAY;
        System.out.print(space + color + symbol + " " + RESET);
    }

    /**
     * Shows a totem (special piece) on the board.
     * @param totem the totem to show
     * @param space spacing for display
     */
    private void displayTotem(Totem totem, String space) {
        String symbol = totem.getSymbol() == Symbol.O ? "O" : "X";
        // Totems are always shown in blue
        System.out.print(space + BLUE + symbol + " " + RESET);
    }

    /**
     * Shows who won the game.
     * @param winner the player who won, or null if it's a tie
     */
    public void displayWinner(Player winner) {
        if (winner != null) {
            String color = winner.getColor() == Color.PINK ? PINK : GRAY;
            System.out.println("WINNER: " + color + "  Player " + winner.getColor() + RESET);
        } else {
            System.out.println("EQUALITY...");
        }
    }

    /**
     * Shows how many tokens a player has left.
     * @param player the player to show tokens for
     */
    public void displayRemainingTokens(Player player) {
        String color = player.getColor() == Color.PINK ? PINK : GRAY;
        System.out.println("Remaining tokens for " + color + "  Player " + player.getColor() + "\tO: " + player.getRemainingTokens(Symbol.O) + "\tX: " + player.getRemainingTokens(Symbol.X) + RESET);
    }

    /**
     * Shows whose turn it is to play.
     * @param currentPlayer the player who plays now
     */
    public void displayCurrentPlayer(Player currentPlayer) {
        String color = currentPlayer.getColor() == Color.PINK ? PINK : GRAY;
        System.out.println("Current turn: " + color + "  Player " + currentPlayer.getColor() + RESET);
    }

    /**
     * Shows what phase the game is in.
     * @param phase the current game phase
     */
    public void displayPhase(GamePhase phase) {
        System.out.println("Current phase: " + phase);
    }

    /**
     * Shows a message to the player.
     * @param message the text to show
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Sets the game that this view will show.
     * @param game the game to display
     */
    public void setGameFacade(GameFacade game) {
        this.game = game;
    }

    /**
     * Turns off colors and uses simple letters instead.
     * Makes the display work better on some computers.
     */
    private void desactivatedColor() {
        RESET = "";
        PINK  = "P";
        BLUE  = "#";
        GRAY  = "B";
    }

    /**
     * Updates the display when something changes in the game.
     * This method runs automatically when the game changes.
     */
    @Override
    public void update() {
        desactivatedColor(); // Use simple letters instead of colors

        if (game == null) {
            displayMessage("Game is not initialized.");
            return;
        }

        // Show different things based on game state
        if (game.getWinner() != null || game.getGamePhase() == GamePhase.GAME_OVER) {
            // Game is over - show final board and winner
            displayBoard(game.getBoardState(), game.getBoardSize(), null);
            displayWinner(game.getWinner());
        } else {
            // Game is still playing - show current state
            displayRemainingTokens(game.getPinkPlayer());
            displayRemainingTokens(game.getBlackPlayer());
            displayBoard(game.getBoardState(), game.getBoardSize(), game.getValidPositions());
            displayCurrentPlayer(game.getCurrentPlayer());
            displayPhase(game.getGamePhase());
        }
    }
}
