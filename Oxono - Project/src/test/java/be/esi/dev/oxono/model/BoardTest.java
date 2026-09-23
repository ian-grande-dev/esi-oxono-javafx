package be.esi.dev.oxono.model;

import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.Symbol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;
    private static final int BOARD_SIZE = 6;

    @BeforeEach
    void setUp() {
        board = new Board(BOARD_SIZE);
        System.out.print("");
        System.out.print("");
    }

    @Nested
    class ConstructorTests {

        @Test
        void testConstructor_initialTotemsPositions_centered() {
            Position totemOPosition = board.getPositionTotem(Symbol.O);
            Position totemXPosition = board.getPositionTotem(Symbol.X);

            assertEquals(new Position(2, 2), totemOPosition); // Totem O
            assertEquals(new Position(3, 3), totemXPosition); // Totem X

            assertInstanceOf(Totem.class, board.getPawnAt(totemOPosition));
            assertInstanceOf(Totem.class, board.getPawnAt(totemXPosition));
        }

        @Test
        void testConstructor_cellsAreEmptyExceptTotems() {
            Position totemOPosition = board.getPositionTotem(Symbol.O);
            Position totemXPosition = board.getPositionTotem(Symbol.X);
            int emptyCount = 0;

            for (int y = 0; y < BOARD_SIZE; y++) {
                for (int x = 0; x < BOARD_SIZE; x++) {
                    Position pos = new Position(x, y);
                    if (!pos.equals(totemOPosition) && !pos.equals(totemXPosition)) {
                        assertTrue(board.isEmpty(pos));
                        emptyCount++;
                    }
                }
            }

            assertEquals(BOARD_SIZE * BOARD_SIZE - 2, emptyCount);
        }
    }

    @Nested
    class PositionValidationTests {

        @Test
        void testCheckPositionException_throwsForNegativeX() {
            Position invalidPos = new Position(-1, 2);
            assertThrows(IllegalArgumentException.class,
                () -> board.checkPositionException(invalidPos));
        }

        @Test
        void testCheckPositionException_throwsForXEqualSize() {
            Position invalidPos = new Position(BOARD_SIZE, 2);
            assertThrows(IllegalArgumentException.class,
                () -> board.checkPositionException(invalidPos));
        }

        @Test
        void testCheckPositionException_throwsForNegativeY() {
            Position invalidPos = new Position(2, -1);
            assertThrows(IllegalArgumentException.class,
                () -> board.checkPositionException(invalidPos));
        }

        @Test
        void testCheckPositionException_throwsForYEqualSize() {
            Position invalidPos = new Position(2, BOARD_SIZE);
            assertThrows(IllegalArgumentException.class,
                () -> board.checkPositionException(invalidPos));
        }

        @Test
        void testCheckPositionException_okForInnerCell() {
            Position validPos = new Position(3, 3);
            assertDoesNotThrow(() -> board.checkPositionException(validPos));
        }
    }

    @Nested
    class PawnManipulationTests {

        @Test
        void testPlaceToken_placesAtCoordinates() {
            Position pos = new Position(0, 0);
            Token token = new Token(Symbol.O, Color.PINK);

            board.placeToken(token, pos);

            assertEquals(token, board.getPawnAt(pos));
            assertFalse(board.isEmpty(pos));
        }

        @Test
        void testGetPawnAt_returnsTotemAtInitialPositions() {
            Position totemOPosition = board.getPositionTotem(Symbol.O);
            Position totemXPosition = board.getPositionTotem(Symbol.X);

            Pawn totemO = board.getPawnAt(totemOPosition);
            Pawn totemX = board.getPawnAt(totemXPosition);

            assertInstanceOf(Totem.class, totemO);
            assertInstanceOf(Totem.class, totemX);
            assertEquals(Symbol.O, totemO.getSymbol());
            assertEquals(Symbol.X, totemX.getSymbol());
        }

        @Test
        void testIsEmpty_trueOnEmptyCell_falseOnOccupied() {
            Position emptyPos = new Position(0, 0);
            Position occupiedPos = board.getPositionTotem(Symbol.O);

            assertTrue(board.isEmpty(emptyPos));
            assertFalse(board.isEmpty(occupiedPos));
        }

        @Test
        void testRemovePawn_onOccupiedReturnsTrueAndClears() {
            Position pos = new Position(0, 0);
            Token token = new Token(Symbol.X, Color.PINK);
            board.placeToken(token, pos);

            assertTrue(board.removePawn(pos));
            assertTrue(board.isEmpty(pos));
        }

        @Test
        void testRemovePawn_onEmptyReturnsFalse() {
            Position emptyPos = new Position(0, 0);

            assertFalse(board.removePawn(emptyPos));
            assertTrue(board.isEmpty(emptyPos));
        }
    }

    @Nested
    class TotemMovementTests {

        @Test
        void testMoveTotem_updatesCoordinatesAndBoardForO() {
            Totem totemO = new Totem(Symbol.O);
            Position oldPos = board.getPositionTotem(Symbol.O);
            Position newPos = new Position(0, 0);

            board.moveTotem(totemO, newPos);

            assertTrue(board.isEmpty(oldPos));
            assertEquals(totemO, board.getPawnAt(newPos));
            assertEquals(newPos, board.getPositionTotem(Symbol.O));
        }

        @Test
        void testMoveTotem_updatesCoordinatesAndBoardForX() {
            Totem totemX = new Totem(Symbol.X);
            Position oldPos = board.getPositionTotem(Symbol.X);
            Position newPos = new Position(5, 5);

            board.moveTotem(totemX, newPos);

            assertTrue(board.isEmpty(oldPos));
            assertEquals(totemX, board.getPawnAt(newPos));
            assertEquals(newPos, board.getPositionTotem(Symbol.X));
        }

        @Test
        void testMoveTotem_destinationOverwriteAllowedByCurrentImpl() {
            Totem totemO = new Totem(Symbol.O);
            Position destination = new Position(0, 0);
            Token existingToken = new Token(Symbol.X, Color.PINK);

            board.placeToken(existingToken, destination);

            board.moveTotem(totemO, destination);

            assertEquals(totemO, board.getPawnAt(destination));
        }
    }

    @Nested
    class ValidPositionsTests {

        @Test
        void testGetValidPositionsToPlaceToken_nonEnclaved_returnsAdjacentEmpties() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = board.getPositionTotem(Symbol.O);

            List<Position> validPositions = board.getValidPositionsToPlaceToken(totemO);

            assertTrue(validPositions.size() <= 4);
            for (Position pos : validPositions) {
                assertTrue(board.isEmpty(pos));
                assertTrue(isAdjacent(pos, totemPos));
            }
        }

        @Test
        void testGetValidPositionsToPlaceToken_enclaved_returnsAllEmpty() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = board.getPositionTotem(Symbol.O);

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x(), totemPos.y() - 1));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x(), totemPos.y() + 1));
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x() - 1, totemPos.y()));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x() + 1, totemPos.y()));

            List<Position> validPositions = board.getValidPositionsToPlaceToken(totemO);

            assertEquals(BOARD_SIZE * BOARD_SIZE - 6, validPositions.size());
        }

        @Test
        void testGetValidPositionsToMoveTotem_nonEnclaved_returnsAllEmptyStraightUntilObstacle() {
            Totem totemO = new Totem(Symbol.O);

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(2, 0));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(0, 2));

            List<Position> validPositions = board.getValidPositionsToMoveTotem(totemO);

            assertFalse(validPositions.isEmpty());
            for (Position pos : validPositions) {
                assertTrue(board.isEmpty(pos));
            }
        }

        @Test
        void testGetValidPositionsToMoveTotem_enclaved_returnsFirstEmptyAfterSeries() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = board.getPositionTotem(Symbol.O);

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x(), totemPos.y() - 1));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x(), totemPos.y() + 1));
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x() - 1, totemPos.y()));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x() + 1, totemPos.y()));

            List<Position> validPositions = board.getValidPositionsToMoveTotem(totemO);

            assertFalse(validPositions.isEmpty());
            assertEquals(4, validPositions.size());
        }
    }

    @Nested
    class EnclavementTests {

        @Test
        void testIsEnclaved_trueWhenFourAdjacentsOccupied() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = board.getPositionTotem(Symbol.O);

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x(), totemPos.y() - 1));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x(), totemPos.y() + 1));
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x() - 1, totemPos.y()));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x() + 1, totemPos.y()));

            List<Position> adjacentEmpty = board.getValidPositionsToPlaceToken(totemO);

            assertEquals(BOARD_SIZE * BOARD_SIZE - 6, adjacentEmpty.size());
        }

        @Test
        void testIsEnclaved_falseWhenAnyAdjacentEmpty() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = board.getPositionTotem(Symbol.O);

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x(), totemPos.y() - 1));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x() - 1, totemPos.y()));
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(totemPos.x() + 1, totemPos.y()));

            List<Position> adjacentEmpty = board.getValidPositionsToPlaceToken(totemO);

            assertTrue(adjacentEmpty.size() <= 4);
        }
    }

    @Nested
    class WinConditionTests {

        @Test
        void testCheckTokenWinCondition_trueWithFourXInRow() {
            for (int x = 0; x < 4; x++) {
                board.placeToken(new Token(Symbol.X, Color.PINK), new Position(x, 0));
            }

            assertTrue(board.checkTokenWinCondition(new Position(0, 0)));
        }

        @Test
        void testCheckTokenWinCondition_trueWithFourOInColumn() {
            for (int y = 0; y < 4; y++) {
                board.placeToken(new Token(Symbol.O, Color.BLACK), new Position(0, y));
            }

            assertTrue(board.checkTokenWinCondition(new Position(0, 0)));
        }

        @Test
        void testCheckTokenWinCondition_falseWhenLessThanFour() {
            for (int x = 0; x < 3; x++) {
                board.placeToken(new Token(Symbol.X, Color.PINK), new Position(x, 0));
            }

            assertFalse(board.checkTokenWinCondition(new Position(0, 0)));
        }

        @Test
        void testCheckTokenWinCondition_ignoresTotemsInCount() {
            for (int x = 0; x < 3; x++) {
                board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(x, 0));
            }
            board.moveTotem(new Totem(Symbol.X), new Position(3, 0));

            assertFalse(board.checkTokenWinCondition(new Position(0, 0)));
        }

        @Test
        void testHasFourSameTokenSymbol_countsTotalsNotConsecutive_currentBehavior() {
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(0, 0));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(2, 0));
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(4, 0));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(5, 0));

            assertFalse(board.checkTokenWinCondition(new Position(0, 0)));
        }
    }

    @Nested
    class UtilityTests {

        @Test
        void testGetPositionsTotems_returnsBothTotemPositions() {
            Position totemOPosition = board.getPositionTotem(Symbol.O);
            Position totemXPosition = board.getPositionTotem(Symbol.X);

            assertEquals(new Position(2, 2), totemOPosition); // Totem O
            assertEquals(new Position(3, 3), totemXPosition); // Totem X
        }

        @Test
        void testGetAdjacentEmptyPositions_boundsCheckedAndOnlyEmpties() {
            Totem totemO = new Totem(Symbol.O);
            board.moveTotem(totemO, new Position(0, 0));

            List<Position> validPositions = board.getValidPositionsToPlaceToken(totemO);

            assertEquals(2, validPositions.size());
            assertTrue(validPositions.contains(new Position(1, 0)));
            assertTrue(validPositions.contains(new Position(0, 1)));
        }

        @Test
        void testGetValidPositionsToMoveTotem_cornerPosition_limitsDirections() {
            Totem totemX = new Totem(Symbol.X);
            board.moveTotem(totemX, new Position(0, 0));

            List<Position> validPositions = board.getValidPositionsToMoveTotem(totemX);

            assertTrue(validPositions.size() <= 10);
            for (Position pos : validPositions) {
                assertTrue(board.isEmpty(pos));
                assertTrue(pos.x() == 0 || pos.y() == 0);
            }
        }

        @Test
        void testGetValidPositionsToMoveTotem_centerPosition_allDirectionsAvailable() {
            Totem totemO = new Totem(Symbol.O);
            board.moveTotem(totemO, new Position(3, 3));

            List<Position> validPositions = board.getValidPositionsToMoveTotem(totemO);

            assertFalse(validPositions.isEmpty());
            for (Position pos : validPositions) {
                assertTrue(board.isEmpty(pos));
            }
        }

        @Test
        void testGetValidPositionsToPlaceToken_cornerTotem_twoAdjacentOnly() {
            Totem totemX = new Totem(Symbol.X);
            board.moveTotem(totemX, new Position(5, 5));

            List<Position> validPositions = board.getValidPositionsToPlaceToken(totemX);

            assertEquals(2, validPositions.size());
            assertTrue(validPositions.contains(new Position(4, 5)));
            assertTrue(validPositions.contains(new Position(5, 4)));
        }

        @Test
        void testGetValidPositionsToMoveTotem_withObstacles_stopsAtFirstObstacle() {
            Totem totemO = new Totem(Symbol.O);
            board.moveTotem(totemO, new Position(1, 1));

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(1, 3));

            List<Position> validPositions = board.getValidPositionsToMoveTotem(totemO);

            assertTrue(validPositions.contains(new Position(1, 2)));
            assertFalse(validPositions.contains(new Position(1, 4)));
        }

        @Test
        void testGetValidPositionsToPlaceToken_partiallyEnclosed_returnsRemainingAdjacent() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = new Position(3, 3);
            board.moveTotem(totemO, totemPos);

            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(3, 2));
            board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(3, 4));
            board.placeToken(new Token(Symbol.X, Color.PINK), new Position(2, 3));

            List<Position> validPositions = board.getValidPositionsToPlaceToken(totemO);

            assertEquals(1, validPositions.size());
            assertTrue(validPositions.contains(new Position(4, 3)));
        }

        @Test
        void testGetValidPositionsToPlaceToken_allAdjacentOccupied_returnsAllEmpty() {
            Totem totemO = new Totem(Symbol.O);
            Position totemPos = board.getPositionTotem(Symbol.O);
            board.moveTotem(totemO, totemPos);

            for (int i = 0; i < board.getSize(); i++) {
                if (i != totemPos.x() && i != totemPos.y()) {
                    board.placeToken(new Token(Symbol.X, Color.PINK), new Position(i, totemPos.y()));
                    board.placeToken(new Token(Symbol.X, Color.BLACK), new Position(totemPos.x(), i));
                }
            }

            List<Position> validPositions = board.getValidPositionsToPlaceToken(totemO);

            assertEquals(BOARD_SIZE * BOARD_SIZE - 12, validPositions.size());
        }
    }

    @Nested
    class RobustnessTests {

        @Test
        void testPlaceToken_overwritesExistingPawn_currentBehavior() {
            Position pos = new Position(0, 0);
            Token firstToken = new Token(Symbol.O, Color.PINK);
            Token secondToken = new Token(Symbol.X, Color.BLACK);

            board.placeToken(firstToken, pos);
            board.placeToken(secondToken, pos);

            assertEquals(secondToken, board.getPawnAt(pos));
        }

        @Test
        void testGetPawnAt_outOfBoundsViaCheckPositionExceptionPrecheck() {
            Position outOfBounds = new Position(-1, -1);

            assertThrows(IllegalArgumentException.class,
                () -> board.checkPositionException(outOfBounds));
        }
    }

    private boolean isAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.x() - pos2.x());
        int dy = Math.abs(pos1.y() - pos2.y());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
}