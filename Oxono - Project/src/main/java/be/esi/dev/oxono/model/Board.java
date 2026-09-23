package be.esi.dev.oxono.model;

import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the game board where pieces are placed.
 * This class manages the grid and all game pieces on it.
 */
public class Board {
    // The game board as a 2D array
    private final Pawn[][] board;
    // Position of the O totem on the board
    private Position positionTotemO;
    // Position of the X totem on the board
    private Position positionTotemX;

    /**
     * Creates a new game board with the given size.
     * Places the two totems in the middle of the board.
     * @param size the number of rows and columns for the board
     */
    protected Board(int size) {
        board = new Pawn[size][size];

        // Calculate middle positions for totems
        int middle = size / 2;
        positionTotemO = new Position(middle - 1, middle - 1);
        positionTotemX = new Position(middle + (size % 2), middle + (size % 2));

        // Place totems on the board
        placeTotem(new Totem(Symbol.O), positionTotemO);
        placeTotem(new Totem(Symbol.X), positionTotemX);
    }

    /**
     * Creates a board from an existing game state.
     * Used to copy or restore a game board.
     * @param boardMap map of positions and pieces
     */
    protected Board(Map<Position, Pawn> boardMap) {
        int size = (int) Math.sqrt(boardMap.size());
        this.board = new Pawn[size][size];

        // Copy all pieces from the map to the board
        for (Map.Entry<Position, Pawn> entry : boardMap.entrySet()) {
            Position position = entry.getKey();
            Pawn pawn = entry.getValue();
            board[position.y()][position.x()] = pawn;

            // Remember totem positions
            if (pawn instanceof Totem) {
                Totem totem = (Totem) pawn;
                if (totem.getSymbol() == Symbol.O) {
                    positionTotemO = position;
                } else if (totem.getSymbol() == Symbol.X) {
                    positionTotemX = position;
                }
            }
        }
    }

    /**
     * Moves a totem to a new position on the board.
     * @param totem the totem to move
     * @param position the new position for the totem
     */
    protected void moveTotem(Totem totem, Position position) {
        Position totemPosition = getPositionTotem(totem.getSymbol());
        removePawn(totemPosition); // Remove totem from old position

        // Update totem position
        if (totem.getSymbol() == Symbol.O) {
            positionTotemO = position;
        } else if (totem.getSymbol() == Symbol.X) {
            positionTotemX = position;
        } else {
            throw new IllegalArgumentException("Invalid totem symbol: " + totem.getSymbol());
        }
        placeTotem(totem, position); // Place totem in new position
    }

    /**
     * Places a token on the board at the given position.
     * @param token the token to place
     * @param position where to place the token
     */
    protected void placeToken(Token token, Position position) {
        board[position.y()][position.x()] = token;
    }

    /**
     * Removes a piece from the board at the given position.
     * @param position where to remove the piece
     * @return true if a piece was removed, false if position was empty
     */
    protected boolean removePawn(Position position) {
        if (!isEmpty(position)) {
            board[position.y()][position.x()] = null;
            return true;
        }
        return false;
    }

    /**
     * Checks if a position on the board is empty.
     * @param position the position to check
     * @return true if empty, false if there is a piece
     */
    protected boolean isEmpty(Position position) {
        return board[position.y()][position.x()] == null;
    }

    /**
     * Gets the current position of a totem.
     * @param symbol the symbol of the totem (O or X)
     * @return the position of the totem
     */
    protected Position getPositionTotem(Symbol symbol) {
        if (symbol == Symbol.O) {
            return positionTotemO;
        } else if (symbol == Symbol.X) {
            return positionTotemX;
        } else {
            throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }
    }

    /**
     * Gets all valid positions where a totem can be moved.
     * @param totem the totem to move
     * @return list of valid positions
     */
    protected List<Position> getValidPositionsToMoveTotem(Totem totem) {
        Position totemPosition = getPositionTotem(totem.getSymbol());

        // If totem is not blocked, it can move in straight lines
        if (isNotEnclaved(totem)) {
            return getAllEmptyPositionsInDirections(totemPosition);
        }

        // If totem is blocked, try to find first empty positions
        List<Position> validPositions = getFirstEmptyPositionsInDirections(totemPosition);
        if (!validPositions.isEmpty()) {
            return validPositions;
        }

        // If no positions found, can move anywhere empty
        return getAllEmptyPositions();
    }

    /**
     * Gets all valid positions where a token can be placed.
     * @param totem the totem that will place the token
     * @return list of valid positions
     */
    protected List<Position> getValidPositionsToPlaceToken(Totem totem) {
        Position totemPosition = getPositionTotem(totem.getSymbol());
        if (isNotEnclaved(totem)) {
            // Token can only be placed next to the totem
            return getAdjacentEmptyPositions(totemPosition);
        }
        // If totem is blocked, token can be placed anywhere
        return getAllEmptyPositions();
    }

    /**
     * Gets the piece at a specific position.
     * @param position the position to check
     * @return the piece at that position, or null if empty
     */
    protected Pawn getPawnAt(Position position) {
        return board[position.y()][position.x()];
    }

    /**
     * Checks if placing a token at this position wins the game.
     * @param position where the token was placed
     * @return true if this move wins the game
     */
    protected boolean checkTokenWinCondition(Position position) {
        // Check vertical and horizontal lines for winning combinations
        List<Position> positionsV = getVerticalPositions(position);
        List<Position> positionsH = getHorizontalPositions(position);

        if (hasSameConsecutiveToken(positionsV) || hasSameConsecutiveToken(positionsH)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a position is valid (inside the board).
     * @param position the position to check
     * @throws IllegalArgumentException if position is outside the board
     */
    protected void checkPositionException(Position position) {
        if (position.x() < 0 || position.x() >= board.length || position.y() < 0 || position.y() >= board.length) {
            throw new IllegalArgumentException("Position out of bounds: " + position);
        }
    }

    /**
     * Gets the size of the board.
     * @return the number of rows (same as columns)
     */
    protected int getSize() {
        return board.length;
    }

    /**
     * Checks if a totem is not blocked by other pieces.
     * @param totem the totem to check
     * @return true if totem can move freely, false if blocked
     */
    private boolean isNotEnclaved(Totem totem) {
        Position totemPosition = getPositionTotem(totem.getSymbol());
        return !getAdjacentEmptyPositions(totemPosition).isEmpty();
    }

    /**
     * Gets all empty positions next to a given position.
     * @param position the center position
     * @return list of adjacent empty positions
     */
    private List<Position> getAdjacentEmptyPositions(Position position) {
        List<Position> adjacentPositions = new ArrayList<>();
        int x = position.x();
        int y = position.y();
        int size = board.length;

        // Check four directions: up, down, left, right
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] direction : directions) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            // Check if new position is inside the board
            if (newX >= 0 && newX < size && newY >= 0 && newY < size) {
                Position adjacentPosition = new Position(newX, newY);
                if (isEmpty(adjacentPosition)) {
                    adjacentPositions.add(adjacentPosition);
                }
            }
        }
        return List.copyOf(adjacentPositions);
    }

    /**
     * Gets all empty positions on the board.
     * @return list of all empty positions
     */
    private List<Position> getAllEmptyPositions() {
        List<Position> emptyPositions = new ArrayList<>();
        int size = board.length;

        // Check every position on the board
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Position position = new Position(x, y);
                if (isEmpty(position)) {
                    emptyPositions.add(position);
                }
            }
        }
        return List.copyOf(emptyPositions);
    }

    /**
     * Gets the first empty position in each direction from a starting point.
     * @param position the starting position
     * @return list of first empty positions in each direction
     */
    private List<Position> getFirstEmptyPositionsInDirections(Position position) {
        List<Position> positions = new ArrayList<>();
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int boardSize = board.length;

        for (int[] direction : directions) {
            int x = position.x() + direction[0];
            int y = position.y() + direction[1];

            // Move in this direction until we find an empty space or hit the edge
            while (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                if (isEmpty(new Position(x, y))) {
                    positions.add(new Position(x, y));
                    break; // Only take the first empty position
                }
                x += direction[0];
                y += direction[1];
            }
        }
        return List.copyOf(positions);
    }

    /**
     * Gets all empty positions in straight lines from a starting point.
     * @param position the starting position
     * @return list of all empty positions in each direction
     */
    private List<Position> getAllEmptyPositionsInDirections(Position position) {
        List<Position> list = new ArrayList<>();
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int boardSize = board.length;

        for (int[] direction : directions) {
            int x = position.x() + direction[0];
            int y = position.y() + direction[1];

            // Move in this direction and collect all empty positions
            while (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                Position currentPosition = new Position(x, y);
                if (isEmpty(currentPosition)) {
                    list.add(currentPosition);
                } else {
                    break; // Stop when we hit a piece
                }
                x += direction[0];
                y += direction[1];
            }
        }
        return List.copyOf(list);
    }

    /**
     * Gets all positions in the same column as the given position.
     * @param position the reference position
     * @return list of all positions in the same column
     */
    private List<Position> getVerticalPositions(Position position) {
        List<Position> positions = new ArrayList<>();
        int x = position.x();
        int size = board.length;

        for (int y = 0; y < size; y++) {
            positions.add(new Position(x, y));
        }
        return List.copyOf(positions);
    }

    /**
     * Gets all positions in the same row as the given position.
     * @param position the reference position
     * @return list of all positions in the same row
     */
    private List<Position> getHorizontalPositions(Position position) {
        List<Position> positions = new ArrayList<>();
        int y = position.y();
        int size = board.length;

        for (int x = 0; x < size; x++) {
            positions.add(new Position(x, y));
        }
        return List.copyOf(positions);
    }

    /**
     * Checks if there are 4 tokens in a row of same symbol or color.
     * @param positions the line of positions to check
     * @return true if there are 4 consecutive matching tokens
     */
    private boolean hasSameConsecutiveToken(List<Position> positions) {
        boolean sameSymbol = hasConsecutiveSymbol(positions, Symbol.O, 4) || hasConsecutiveSymbol(positions, Symbol.X, 4);
        boolean sameColor = hasConsecutiveColor(positions, Color.PINK, 4) || hasConsecutiveColor(positions, Color.BLACK, 4);
        return sameSymbol || sameColor;
    }

    /**
     * Checks for consecutive tokens with the same symbol.
     * @param positions the positions to check
     * @param symbol the symbol to look for
     * @param requiredCount how many in a row are needed
     * @return true if found enough consecutive tokens
     */
    private boolean hasConsecutiveSymbol(List<Position> positions, Symbol symbol, int requiredCount) {
        int currentCount = 0;
        int maxCount = 0;

        for (Position pos : positions) {
            Pawn pawn = getPawnAt(pos);
            if (pawn instanceof Token && pawn.getSymbol() == symbol) {
                currentCount++;
                maxCount = Math.max(maxCount, currentCount);
            } else {
                currentCount = 0; // Reset count when we don't find matching token
            }
        }
        return maxCount >= requiredCount;
    }

    /**
     * Checks for consecutive tokens with the same color.
     * @param positions the positions to check
     * @param color the color to look for
     * @param requiredCount how many in a row are needed
     * @return true if found enough consecutive tokens
     */
    private boolean hasConsecutiveColor(List<Position> positions, Color color, int requiredCount) {
        int currentCount = 0;
        int maxCount = 0;

        for (Position pos : positions) {
            Pawn pawn = getPawnAt(pos);
            if (pawn instanceof Token && ((Token) pawn).getColor() == color) {
                currentCount++;
                maxCount = Math.max(maxCount, currentCount);
            } else {
                currentCount = 0; // Reset count when we don't find matching token
            }
        }
        return maxCount >= requiredCount;
    }

    /**
     * Places a totem on the board at the given position.
     * @param totem the totem to place
     * @param position where to place the totem
     */
    private void placeTotem(Totem totem, Position position) {
        board[position.y()][position.x()] = totem;
    }
}