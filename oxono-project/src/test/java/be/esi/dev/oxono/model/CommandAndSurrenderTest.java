package be.esi.dev.oxono.model;

import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.GamePhase;
import be.esi.dev.oxono.util.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommandAndSurrenderTest {

    private GameFacade gameFacade;
    private GameConfiguration config;

    @BeforeEach
    void setUp() {
        // Arrange
        Player pinkPlayer = new Human(Color.PINK, 10);
        Player blackPlayer = new Human(Color.BLACK, 10);
        config = new GameConfiguration(pinkPlayer, blackPlayer, 6);
        gameFacade = new GameFacade(config);
    }

    @Nested
    class UndoTests {

        @Test
        void testUndo_afterChooseTotem_returnsToChooseTotemPhase() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            gameFacade.handlePlayerAction(totemPosition);

            // Act
            gameFacade.undo();

            // Assert
            assertEquals(GamePhase.CHOOSE_TOTEM, gameFacade.getGamePhase());
            assertNull(gameFacade.getSelectedTotem());
        }

        @Test
        void testUndo_afterMoveTotem_returnsToMoveTotemPhase() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);
            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);

            // Act
            gameFacade.undo();

            // Assert
            assertEquals(GamePhase.MOVE_TOTEM, gameFacade.getGamePhase());
            assertEquals(Symbol.O, gameFacade.getSelectedTotem().getSymbol());
            assertEquals(totemPosition, getTotemPosition(Symbol.O));
        }

        @Test
        void testUndo_afterPlaceToken_returnsToPlaceTokenPhase() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);
            Position tokenPosition = new Position(0, 2);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);
            gameFacade.handlePlayerAction(tokenPosition);

            // Act
            gameFacade.undo();

            // Assert
            assertEquals(GamePhase.PLACE_TOKEN, gameFacade.getGamePhase());
            assertEquals(Symbol.O, gameFacade.getSelectedTotem().getSymbol());
            assertTrue(gameFacade.getBoardState().get(tokenPosition) == null);
        }

        @Test
        void testUndo_withoutCommands_throwsException() {
            // Arrange - no action taken

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameFacade.undo());
            assertEquals("No command to undo.", exception.getMessage());
        }

        @Test
        void testUndo_multipleActions_undoesInCorrectOrder() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);

            gameFacade.handlePlayerAction(totemPosition); // Choose totem
            gameFacade.handlePlayerAction(newTotemPosition); // Move totem

            // Act
            gameFacade.undo(); // Undo move
            gameFacade.undo(); // Undo choose

            // Assert
            assertEquals(GamePhase.CHOOSE_TOTEM, gameFacade.getGamePhase());
            assertNull(gameFacade.getSelectedTotem());
        }
    }

    @Nested
    class RedoTests {

        @Test
        void testRedo_afterUndo_redoesChooseTotem() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.undo();

            // Act
            gameFacade.redo();

            // Assert
            assertEquals(GamePhase.MOVE_TOTEM, gameFacade.getGamePhase());
            assertEquals(Symbol.O, gameFacade.getSelectedTotem().getSymbol());
        }

        @Test
        void testRedo_afterUndoMoveTotem_redoesMoveTotem() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);
            gameFacade.undo();

            // Act
            gameFacade.redo();

            // Assert
            assertEquals(GamePhase.PLACE_TOKEN, gameFacade.getGamePhase());
            assertEquals(newTotemPosition, getTotemPosition(Symbol.O));
        }

        @Test
        void testRedo_withoutUndoCommands_throwsException() {
            // Arrange - no action taken

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameFacade.redo());
            assertEquals("No command to redo.", exception.getMessage());
        }

        @Test
        void testRedo_afterNewAction_clearsRedoStack() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);
            gameFacade.undo();
            gameFacade.undo(); // Now in CHOOSE_TOTEM phase

            // New action to empty the redo stack
            Position anotherTotemPosition = new Position(3, 3);
            gameFacade.handlePlayerAction(anotherTotemPosition);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameFacade.redo());
            assertEquals("No command to redo.", exception.getMessage());
        }

        @Test
        void testRedo_multipleUndoRedo_maintainsCorrectState() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);

            gameFacade.undo(); // Undo move
            gameFacade.undo(); // Undo choose

            // Act
            gameFacade.redo(); // Redo choose
            gameFacade.redo(); // Redo move

            // Assert
            assertEquals(GamePhase.PLACE_TOKEN, gameFacade.getGamePhase());
            assertEquals(Symbol.O, gameFacade.getSelectedTotem().getSymbol());
            assertEquals(newTotemPosition, getTotemPosition(Symbol.O));
        }
    }

    @Nested
    class SurrenderTests {

        @Test
        void testSurrender_pinkPlayerSurrenders_blackPlayerWins() {
            // Arrange - current pink player (default)
            Player initialCurrentPlayer = gameFacade.getCurrentPlayer();
            assertEquals(Color.PINK, initialCurrentPlayer.getColor());

            // Act
            gameFacade.surrender();

            // Assert
            assertEquals(GamePhase.GAME_OVER, gameFacade.getGamePhase());
            assertEquals(Color.BLACK, gameFacade.getWinner().getColor());
        }

        @Test
        void testSurrender_blackPlayerSurrenders_pinkPlayerWins() {
            // Arrange - pass to the black player
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);
            Position tokenPosition = new Position(0, 2);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);
            gameFacade.handlePlayerAction(tokenPosition); // Turn passes to the black player

            assertEquals(Color.BLACK, gameFacade.getCurrentPlayer().getColor());

            // Act
            gameFacade.surrender();

            // Assert
            assertEquals(GamePhase.GAME_OVER, gameFacade.getGamePhase());
            assertEquals(Color.PINK, gameFacade.getWinner().getColor());
        }

        @Test
        void testSurrender_duringChooseTotemPhase_endsGame() {
            // Arrange
            assertEquals(GamePhase.CHOOSE_TOTEM, gameFacade.getGamePhase());

            // Act
            gameFacade.surrender();

            // Assert
            assertEquals(GamePhase.GAME_OVER, gameFacade.getGamePhase());
            assertNotNull(gameFacade.getWinner());
        }

        @Test
        void testSurrender_duringMoveTotemPhase_endsGame() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            gameFacade.handlePlayerAction(totemPosition);
            assertEquals(GamePhase.MOVE_TOTEM, gameFacade.getGamePhase());

            // Act
            gameFacade.surrender();

            // Assert
            assertEquals(GamePhase.GAME_OVER, gameFacade.getGamePhase());
            assertNotNull(gameFacade.getWinner());
        }

        @Test
        void testSurrender_duringPlaceTokenPhase_endsGame() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);
            assertEquals(GamePhase.PLACE_TOKEN, gameFacade.getGamePhase());

            // Act
            gameFacade.surrender();

            // Assert
            assertEquals(GamePhase.GAME_OVER, gameFacade.getGamePhase());
            assertNotNull(gameFacade.getWinner());
        }
    }

    @Nested
    class UndoRedoSurrenderIntegrationTests {

        @Test
        void testUndo_afterSurrender_notPossible() {
            // Arrange
            gameFacade.surrender();

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gameFacade.undo());
            assertEquals("No command to undo.", exception.getMessage());
        }

        @Test
        void testUndoRedo_preservesPlayerTurn() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Player initialPlayer = gameFacade.getCurrentPlayer();

            gameFacade.handlePlayerAction(totemPosition);

            // Act
            gameFacade.undo();
            gameFacade.redo();

            // Assert
            assertEquals(GamePhase.MOVE_TOTEM, gameFacade.getGamePhase());
            assertEquals(initialPlayer.getColor(), gameFacade.getCurrentPlayer().getColor());
        }

        @Test
        void testUndoRedo_preservesTokenCounts() {
            // Arrange
            Position totemPosition = new Position(2, 2);
            Position newTotemPosition = new Position(1, 2);
            Position tokenPosition = new Position(0, 2);

            int initialTokensO = gameFacade.getCurrentPlayer().getRemainingTokens(Symbol.O);

            gameFacade.handlePlayerAction(totemPosition);
            gameFacade.handlePlayerAction(newTotemPosition);
            gameFacade.handlePlayerAction(tokenPosition);

            // Act
            gameFacade.undo();

            // Assert
            assertEquals(initialTokensO, gameFacade.getCurrentPlayer().getRemainingTokens(Symbol.O));
        }
    }

    // Utility method for recovering the position of a totem pole
    private Position getTotemPosition(Symbol symbol) {
        Map<Position, Pawn> boardState = gameFacade.getBoardState();

        for (Position position : boardState.keySet()) {
            Pawn pawn = boardState.get(position);

            if (pawn instanceof Totem) {
                Totem totem = (Totem) pawn;
                if (totem.getSymbol() == symbol) {
                    return position;
                }
            }
        }
        return null;
    }
}