package be.esi.dev.oxono.controller.fxml;

import be.esi.dev.oxono.model.*;
import be.esi.dev.oxono.observer.Observer;
import be.esi.dev.oxono.util.GamePhase;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controls the game board display and user clicks.
 * This class shows the game pieces and handles mouse clicks on the board.
 */
public class BoardController implements Observer {

    @FXML
    private GridPane gameGridPane;
    @FXML
    private Label turnMessageLabel;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private Label errorMessageLabel;

    private GameFacade gameFacade;

    // Delay that simulates the bot thinking, without blocking the UI thread
    private static final Duration BOT_THINKING_DELAY = Duration.millis(750);

    // Prevents scheduling the bot twice while a turn is already pending
    private boolean botTurnPending = false;

    /**
     * Updates the board when the game changes.
     * This method runs when something changes in the game.
     */
    @Override
    public void update() {
        Platform.runLater(() -> {
            if (gameFacade == null) return;

            // Remove all pieces from the board
            clearCells();
            Map<Position, Pawn> pawns = gameFacade.getBoardState();

            // Put each piece back on the board
            pawns.forEach((position, pawn) -> updateCell(position.x(), position.y(), pawn));

            updateTurnMessage();
            updateBoardDisplay();
            checkGameOver();
            triggerBotTurnIfNeeded();
        });
    }

    /**
     * Creates a new game board with the given size.
     * @param size the number of rows and columns for the board
     */
    public void createBoard(int size) {
        // Clear the old board
        gameGridPane.getChildren().clear();
        gameGridPane.getColumnConstraints().clear();
        gameGridPane.getRowConstraints().clear();

        // Calculate how much space each cell gets
        double percent = 100.0 / size;
        for (int i = 0; i < size; i++) {
            gameGridPane.getColumnConstraints().add(createColumnConstraint(percent));
            gameGridPane.getRowConstraints().add(createRowConstraint(percent));
        }

        // Create clickable cells for the board
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                gameGridPane.add(createClickableCell(col, row), col, row);
            }
        }
    }

    /**
     * Creates column settings for the board.
     * @param percentWidth how much width the column should take
     * @return the column settings
     */
    private ColumnConstraints createColumnConstraint(double percentWidth) {
        ColumnConstraints colConst = new ColumnConstraints();
        colConst.setPercentWidth(percentWidth);
        return colConst;
    }

    /**
     * Creates row settings for the board.
     * @param percentHeight how much height the row should take
     * @return the row settings
     */
    private RowConstraints createRowConstraint(double percentHeight) {
        RowConstraints rowConst = new RowConstraints();
        rowConst.setPercentHeight(percentHeight);
        return rowConst;
    }

    /**
     * Updates one cell on the board with a game piece.
     * @param col the column number
     * @param row the row number
     * @param pawn the game piece to show
     */
    private void updateCell(int col, int row, Pawn pawn) {
        if (pawn == null) return;
        String imagePath = getPawnImagePath(pawn);
        try {
            Image pawnImage = loadPawnImage(imagePath);
            placePawn(col, row, pawnImage);
        } catch (Exception e) {
            errorMessageLabel.setText("Unable to load image: " + imagePath);
        }
    }

    /**
     * Gets the image file path for a game piece.
     * @param pawn the game piece
     * @return the path to the image file
     */
    private String getPawnImagePath(Pawn pawn) {
        if (pawn instanceof Token token) {
            return "/images/" + token.getColor().toString().toUpperCase() + "_Token_" + token.getSymbol() + ".png";
        } else if (pawn instanceof Totem totem) {
            return "/images/BLUE_Totem_" + totem.getSymbol() + ".png";
        } else {
            throw new IllegalArgumentException("Unsupported pawn type: " + pawn.getClass().getSimpleName());
        }
    }

    /**
     * Loads an image from a file path.
     * @param imagePath the path to the image file
     * @return the loaded image
     */
    private Image loadPawnImage(String imagePath) {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath), "Image not found: " + imagePath));
    }

    /**
     * Creates a cell that can be clicked by the player.
     * @param col the column number
     * @param row the row number
     * @return the clickable cell
     */
    private StackPane createClickableCell(int col, int row) {
        StackPane cell = new StackPane();

        // Handle mouse clicks on this cell
        cell.setOnMouseClicked(event -> {
            statusMessageLabel.setText("Cell clicked at: (" + col + ", " + row + ")");
            errorMessageLabel.setText("");
            try {
                gameFacade.handlePlayerAction(new Position(col, row));
            }
            catch (Exception e) {
                errorMessageLabel.setText("Error handling cell click: " + e.getMessage());
            }
        });
        return cell;
    }

    /**
     * Places a game piece image in a cell.
     * @param col the column number
     * @param row the row number
     * @param pawnImage the image to place
     */
    private void placePawn(int col, int row, Image pawnImage) {
        Node cellNode = getCellAt(col, row);
        if (!(cellNode instanceof StackPane cell)) return;

        // Create image view and make it fit the cell
        ImageView pawnView = new ImageView(pawnImage);
        pawnView.fitWidthProperty().bind(cell.widthProperty().multiply(0.8));
        pawnView.fitHeightProperty().bind(cell.heightProperty().multiply(0.8));
        pawnView.setPreserveRatio(true);
        cell.getChildren().add(pawnView);
    }

    /**
     * Removes all pieces from the board.
     */
    private void clearCells() {
        for (Node node : gameGridPane.getChildren()) {
            if (node instanceof StackPane) {
                ((StackPane) node).getChildren().clear();
            }
        }
    }

    /**
     * Checks if the game is finished and shows the winner.
     */
    private void checkGameOver() {
        if (gameFacade.getGamePhase() == GamePhase.GAME_OVER) {
            Player winner = gameFacade.getWinner();
            if (winner != null) {
                turnMessageLabel.setText("Game Over ! Winner: Player " + winner.getColor());
                statusMessageLabel.setText("Congratulations to Player " + winner.getColor() + " !");
            } else {
                turnMessageLabel.setText("Game Over ! It's a draw.");
                statusMessageLabel.setText("The game ended in a draw.");
            }
        }
    }

    /**
     * Updates the message showing whose turn it is.
     */
    private void updateTurnMessage() {
        if (turnMessageLabel != null && gameFacade != null) {
            Player currentPlayer = gameFacade.getCurrentPlayer();
            if (currentPlayer != null) {
                turnMessageLabel.setText("Current Turn: Player " + currentPlayer.getColor());
            } else {
                turnMessageLabel.setText("No current player.");
            }
        }
    }

    /**
     * Shows which cells can be clicked by highlighting them.
     */
    private void updateBoardDisplay() {
        // Remove old highlights
        for (Node child : gameGridPane.getChildren()) {
            child.getStyleClass().removeAll("validPosition");
        }

        // Add highlights to valid cells
        List<Position> validPositions = gameFacade.getValidPositions();
        for (Position position : validPositions) {
            Node cell = getCellAt(position.x(), position.y());
            if (cell != null) {
                cell.getStyleClass().add("validPosition");
            }
        }
    }

    /**
     * Finds the cell at a specific position on the board.
     * @param col the column number
     * @param row the row number
     * @return the cell at that position, or null if not found
     */
    private Node getCellAt(int col, int row) {
        for (Node child : gameGridPane.getChildren()) {
            Integer childCol = GridPane.getColumnIndex(child);
            Integer childRow = GridPane.getRowIndex(child);
            if (childCol != null && childRow != null &&
                    childCol == col && childRow == row) {
                return child;
            }
        }
        return null;
    }

    /**
     * Makes the bot play if it's the bot's turn.
     */
    private void triggerBotTurnIfNeeded() {
        if (botTurnPending || !isBotTurn()) {
            return;
        }

        botTurnPending = true;
        displayBotThinking();

        // Wait without freezing the interface, then let the bot play
        PauseTransition thinking = new PauseTransition(BOT_THINKING_DELAY);
        thinking.setOnFinished(event -> {
            botTurnPending = false;
            if (!isBotTurn()) {
                return; // The game changed during the delay (undo, surrender...)
            }
            try {
                Position position = gameFacade.handleBotAction();
                displayBotActionResult(position);
            } catch (Exception ex) {
                displayBotActionError(ex.getMessage());
            }
        });
        thinking.play();
    }

    /**
     * Checks if the bot has to play now.
     * @return true if the game is running and it's the bot's turn
     */
    private boolean isBotTurn() {
        return gameFacade != null
                && gameFacade.getGamePhase() != GamePhase.GAME_OVER
                && gameFacade.shouldTriggerBotAction();
    }

    /**
     * Shows a message that the bot is thinking.
     */
    private void displayBotThinking() {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("Bot is thinking...");
        }
    }

    /**
     * Shows where the bot played.
     * @param position where the bot placed its piece
     */
    private void displayBotActionResult(Position position) {
        if (statusMessageLabel != null && position != null) {
            statusMessageLabel.setText("Bot played at: (" + position.x() + ", " + position.y() + ")");
        }
    }

    /**
     * Shows an error message when the bot fails.
     * @param errorMessage the error that happened
     */
    private void displayBotActionError(String errorMessage) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Bot action failed: " + errorMessage);
        }
    }

    /**
     * Sets the game logic for this board controller.
     * @param gameFacade the game logic to use
     */
    public void setGameFacade(GameFacade gameFacade) {
        this.gameFacade = gameFacade;
        this.gameFacade.registerObserver(this);

        createBoard(this.gameFacade.getBoardSize());
        update();
    }

    /**
     * Sets the labels used to show messages to the player.
     * @param turnLabel shows whose turn it is
     * @param statusLabel shows game status
     * @param errorLabel shows error messages
     */
    public void setMessageLabel(Label turnLabel, Label statusLabel, Label errorLabel) {
        this.turnMessageLabel = turnLabel;
        this.statusMessageLabel = statusLabel;
        this.errorMessageLabel = errorLabel;
    }
}