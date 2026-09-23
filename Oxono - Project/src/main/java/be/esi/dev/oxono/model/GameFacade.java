package be.esi.dev.oxono.model;

import be.esi.dev.oxono.command.Command;
import be.esi.dev.oxono.command.CommandManager;
import be.esi.dev.oxono.observer.Observable;
import be.esi.dev.oxono.observer.Observer;
import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.GamePhase;
import be.esi.dev.oxono.util.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class that controls the entire game.
 * This class handles all game logic and communicates with the user interface.
 */
public class GameFacade implements Observable {
    private final Player pinkPlayer;
    private final Player blackPlayer;
    private Player currentPlayer;
    private Player winner;

    private final Board board;
    private Totem selectedTotem;
    private GamePhase gamePhase;
    private List<Position> validPositions;

    private final CommandManager commandManager;
    private final List<Observer> observers;

    /**
     * Creates a new game with the given settings.
     * @param configuration the game settings to use
     */
    public GameFacade(GameConfiguration configuration) {
        this.pinkPlayer = configuration.pinkPlayer();
        this.blackPlayer = configuration.blackPlayer();
        this.currentPlayer = pinkPlayer; // Pink player starts first
        this.board = new Board(configuration.boardSize());

        winner = null;
        selectedTotem = null;
        gamePhase = GamePhase.CHOOSE_TOTEM; // Game starts with choosing a totem
        validPositions = new ArrayList<>();

        commandManager = new CommandManager();
        observers = new ArrayList<>();

        updateValidPositions();
    }

    /**
     * Creates a copy of an existing game.
     * Used for testing game strategies.
     * @param game the game to copy
     */
    public GameFacade(GameFacade game) {
        this.pinkPlayer = game.getPinkPlayer();
        this.blackPlayer = game.getBlackPlayer();
        this.currentPlayer = game.getCurrentPlayer();
        this.winner = game.getWinner();
        this.board = new Board(game.getBoardState());
        this.selectedTotem = game.getSelectedTotem();
        this.gamePhase = game.gamePhase;
        this.validPositions = new ArrayList<>(game.validPositions);
        this.commandManager = new CommandManager();
        this.observers = new ArrayList<>();
    }

    // ##### Bot action handling : #####

    /**
     * Checks if the bot should play now.
     * @return true if it's a bot's turn and game is not over
     */
    public boolean shouldTriggerBotAction() {
        return currentPlayer.isBot() && gamePhase != GamePhase.GAME_OVER;
    }

    /**
     * Makes the bot choose and execute its next move.
     * @return the position where the bot played
     */
    public Position handleBotAction() {
        if (currentPlayer instanceof Bot bot) {
            Position position = bot.getStrategy().chooseAction(this);
            handlePlayerAction(position);
            return position;
        }
        return null;
    }

    // ##### Player action handling : #####

    /**
     * Handles a player's action at a specific position.
     * @param position where the player wants to act
     */
    public void handlePlayerAction(Position position) {
        if (position == null) {
            return;
        }
        validateAction(position);
        processActionByPhase(position);
        updateGameStateAndNotify();
    }

    /**
     * Checks if the player's action is valid.
     * @param position the position where player wants to act
     * @throws RuntimeException if the action is not allowed
     */
    private void validateAction(Position position) {
        board.checkPositionException(position);
        if (!validPositions.contains(position)) {
            throw new RuntimeException("Invalid position: " + position + " for game phase: " + gamePhase);
        }
    }

    /**
     * Processes the action based on current game phase.
     * @param position where the action happens
     */
    private void processActionByPhase(Position position) {
        switch (gamePhase) {
            case CHOOSE_TOTEM -> processChooseTotem(position);
            case MOVE_TOTEM -> processMoveTotem(position);
            case PLACE_TOKEN -> processPlaceToken(position);
            default -> throw new IllegalStateException("Invalid game phase: " + gamePhase);
        }
    }

    /**
     * Updates the game state and tells observers about changes.
     */
    private void updateGameStateAndNotify() {
        if (winner == null) {
            updateValidPositions();
            checkGameStatus();
        }
        notifyObservers();
    }

    // ##### Undo/Redo and Surrender : #####

    /**
     * Undoes the last action if possible.
     * @throws IllegalStateException if no action can be undone
     */
    public void undo() {
        if (commandManager.canUndo()) {
            commandManager.undo();
            updateValidPositions();
            // If current player is bot, undo their move too
            if (currentPlayer.isBot()) {
                undo();
            }
            notifyObservers();
        }
        else {
            throw new IllegalStateException("No command to undo.");
        }
    }

    /**
     * Redoes the last undone action if possible.
     * @throws IllegalStateException if no action can be redone
     */
    public void redo() {
        if (commandManager.canRedo()) {
            commandManager.redo();
            updateValidPositions();
            // If current player is bot, redo their move too
            if (currentPlayer.isBot()) {
                redo();
            }
            notifyObservers();
        }
        else {
            throw new IllegalStateException("No command to redo.");
        }
    }

    /**
     * Makes the current player surrender the game.
     * The other player becomes the winner.
     */
    public void surrender() {
        winner = (currentPlayer == pinkPlayer) ? blackPlayer : pinkPlayer;
        gamePhase = GamePhase.GAME_OVER;
        notifyObservers();
    }

    // ##### Game phase handling : #####

    /**
     * Processes choosing a totem action.
     * @param position the position of the chosen totem
     */
    private void processChooseTotem(Position position) {
        Command chooseTotemCommand = new ChooseTotemCommand(this, position);
        commandManager.addCommand(chooseTotemCommand);
        handleChooseTotem(position);
    }

    /**
     * Processes moving a totem action.
     * @param position where to move the totem
     */
    private void processMoveTotem(Position position) {
        Position previousTotemPosition = board.getPositionTotem(selectedTotem.getSymbol());
        Command moveTotemCommand = new MoveTotemCommand(this, position, previousTotemPosition);
        commandManager.addCommand(moveTotemCommand);
        handleMoveTotem(position);
    }

    /**
     * Processes placing a token action.
     * @param position where to place the token
     */
    private void processPlaceToken(Position position) {
        Symbol symbol = selectedTotem.getSymbol();
        Command placeTokenCommand = new PlaceTokenCommand(this, position, symbol);
        commandManager.addCommand(placeTokenCommand);
        handlePlaceToken(position);
    }

    /**
     * Goes back to the place token phase (used for undo).
     * @param lastSymbol the symbol of the token that was placed
     * @param lastTokenPosition where the token was placed
     */
    protected void backToPlaceTokenPhase(Symbol lastSymbol, Position lastTokenPosition) {
        gamePhase = GamePhase.PLACE_TOKEN;

        switchCurrentPlayer(); // Switch back to previous player

        // Give the token back to the player
        currentPlayer.giveBackToken(lastSymbol);
        board.removePawn(lastTokenPosition);

        // Re-select the totem that was used
        Position lastTotemPosition = board.getPositionTotem(lastSymbol);
        selectedTotem = validateTotemAtPosition(lastTotemPosition);
    }

    /**
     * Goes back to the choose totem phase (used for undo).
     */
    protected void backToChooseTotemPhase() {
        gamePhase = GamePhase.CHOOSE_TOTEM;
        selectedTotem = null;
    }

    /**
     * Goes back to the move totem phase (used for undo).
     * @param previousTotemPosition where the totem was before moving
     */
    protected void backToMoveTotemPhase(Position previousTotemPosition) {
        gamePhase = GamePhase.MOVE_TOTEM;
        board.moveTotem(selectedTotem, previousTotemPosition);
    }

    /**
     * Moves the selected totem to a new position.
     * @param position where to move the totem
     */
    protected void handleMoveTotem(Position position) {
        board.moveTotem(selectedTotem, position);
        gamePhase = GamePhase.PLACE_TOKEN;
    }

    /**
     * Places a token on the board and checks for win condition.
     * @param position where to place the token
     */
    protected void handlePlaceToken(Position position) {
        board.placeToken(currentPlayer.consumeToken(selectedTotem.getSymbol()), position);

        // Check if this move wins the game
        if (board.checkTokenWinCondition(position)) {
            winner = currentPlayer;
            gamePhase = GamePhase.GAME_OVER;
        } else {
            // Continue game with next player
            switchCurrentPlayer();
            gamePhase = GamePhase.CHOOSE_TOTEM;
            selectedTotem = null;
        }
    }

    /**
     * Chooses which totem the player will use.
     * @param position the position of the totem to choose
     */
    protected void handleChooseTotem(Position position) {
        Totem totem = validateTotemAtPosition(position);
        validatePlayerHasTokens(totem.getSymbol());
        selectedTotem = totem;
        gamePhase = GamePhase.MOVE_TOTEM;
    }

    /**
     * Checks that there is a totem at the given position.
     * @param position the position to check
     * @return the totem at that position
     * @throws IllegalArgumentException if no totem is found
     */
    private Totem validateTotemAtPosition(Position position) {
        Pawn pawn = board.getPawnAt(position);
        if (pawn instanceof Totem totem) {
            return totem;
        }
        throw new IllegalArgumentException("The position " + position + " does not contain a totem.");
    }

    /**
     * Checks that the player has tokens of the given symbol.
     * @param symbol the symbol to check
     * @throws RuntimeException if player has no tokens left
     */
    private void validatePlayerHasTokens(Symbol symbol) {
        if (currentPlayer.getRemainingTokens(symbol) <= 0) {
            throw new RuntimeException("The player " + currentPlayer.getColor() + " has no tokens of type " + symbol + " left.");
        }
    }

    // ##### Private helper methods : #####

    /**
     * Updates the list of valid positions based on current game phase.
     */
    private void updateValidPositions() {
        switch (gamePhase) {
            case CHOOSE_TOTEM -> updateForChooseTotem();
            case MOVE_TOTEM -> validPositions = board.getValidPositionsToMoveTotem(selectedTotem);
            case PLACE_TOKEN -> validPositions = board.getValidPositionsToPlaceToken(selectedTotem);
            default -> throw new IllegalStateException("Invalid game phase: " + gamePhase);
        }
    }

    /**
     * Updates valid positions for choosing a totem.
     * Only shows totems that the player has tokens for.
     */
    private void updateForChooseTotem() {
        List<Position> newValidPositions = new ArrayList<>();

        // Add O totem if player has O tokens
        if (currentPlayer.getRemainingTokens(Symbol.O) > 0) {
            newValidPositions.add(board.getPositionTotem(Symbol.O));
        }
        // Add X totem if player has X tokens
        if (currentPlayer.getRemainingTokens(Symbol.X) > 0) {
            newValidPositions.add(board.getPositionTotem(Symbol.X));
        }
        this.validPositions = newValidPositions;
    }

    /**
     * Changes the current player to the other player.
     */
    private void switchCurrentPlayer() {
        currentPlayer = (currentPlayer == pinkPlayer) ? blackPlayer : pinkPlayer;
    }

    /**
     * Checks if the game should end.
     */
    private void checkGameStatus() {
        if (winner != null) return;

        // If no valid moves, game is over
        if (validPositions.isEmpty()) {
            gamePhase = GamePhase.GAME_OVER;
            return;
        }

        // If both players have no tokens left, game is over
        if (pinkPlayer.hasNoTokensLeft() && blackPlayer.hasNoTokensLeft()) {
            gamePhase = GamePhase.GAME_OVER;
        }
    }

    /**
     * Creates a copy of a player.
     * @param original the player to copy
     * @param color the color for the copy
     * @return a new player with the same properties
     */
    private Player copyPlayer(Player original, Color color) {
        Player copy = null;
        if (original instanceof Human) {
            copy = new Human(color, 0);
        } else if (original instanceof Bot) {
            copy = new Bot(color, 0, original.getStrategy());
        }

        if (copy != null) {
            copy.setRemainingTokensO(original.getRemainingTokens(Symbol.O));
            copy.setRemainingTokensX(original.getRemainingTokens(Symbol.X));
        }
        return copy;
    }

    /**
     * Creates a copy of a game piece.
     * @param original the piece to copy
     * @return a new piece with the same properties
     */
    private Pawn copyPawn(Pawn original) {
        if (original instanceof Token token) {
            return new Token(token.getSymbol(), token.getColor());
        } else if (original instanceof Totem totem) {
            return new Totem(totem.getSymbol());
        }
        return null;
    }

    // ##### Getters for game state : #####

    /**
     * Gets a copy of the pink player.
     * @return copy of the pink player
     */
    public Player getPinkPlayer() {
        return copyPlayer(pinkPlayer, Color.PINK);
    }

    /**
     * Gets a copy of the black player.
     * @return copy of the black player
     */
    public Player getBlackPlayer() {
        return copyPlayer(blackPlayer, Color.BLACK);
    }

    /**
     * Gets a copy of the current player.
     * @return copy of the current player
     */
    public Player getCurrentPlayer() {
        if (currentPlayer == pinkPlayer) {
            return getPinkPlayer();
        } else {
            return getBlackPlayer();
        }
    }

    /**
     * Gets a copy of the winner, if there is one.
     * @return copy of the winner, or null if no winner yet
     */
    public Player getWinner() {
        if (winner == pinkPlayer) {
            return getPinkPlayer();
        } else if (winner == blackPlayer) {
            return getBlackPlayer();
        } else {
            return null;
        }
    }

    /**
     * Gets the current phase of the game.
     * @return the current game phase
     */
    public GamePhase getGamePhase() {
        return gamePhase;
    }

    /**
     * Gets a copy of the list of valid positions.
     * @return list of positions where the player can act
     */
    public List<Position> getValidPositions() {
        return List.copyOf(validPositions);
    }

    /**
     * Gets the size of the game board.
     * @return the number of rows (same as columns)
     */
    public int getBoardSize() {
        return board.getSize();
    }

    /**
     * Gets a copy of the currently selected totem.
     * @return copy of the selected totem, or null if none selected
     */
    public Totem getSelectedTotem() {
        if (selectedTotem == null) {
            return null;
        }
        return new Totem(selectedTotem.getSymbol());
    }

    /**
     * Gets a copy of the current state of the board.
     * @return map of all positions and their pieces
     */
    public Map<Position, Pawn> getBoardState() {
        Map<Position, Pawn> boardState = new HashMap<>();
        int size = board.getSize();

        // Copy every position and piece on the board
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Position position = new Position(j, i);
                Pawn pawn = board.getPawnAt(position);
                boardState.put(position, copyPawn(pawn));
            }
        }
        return boardState;
    }

    // ##### Observable implementation : #####

    /**
     * Adds an observer to watch this game.
     * @param observer the observer to add
     */
    @Override
    public void registerObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes an observer from watching this game.
     * @param observer the observer to remove
     */
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Tells all observers that something changed in the game.
     */
    private void notifyObservers() {
        for (Observer observer : observers) {
            try {
                observer.update();
            } catch (Exception e) {
                System.err.println("Error updating observer: " + e.getMessage());
            }
        }
    }
}
