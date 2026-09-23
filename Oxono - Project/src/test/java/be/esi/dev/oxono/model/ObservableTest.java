package be.esi.dev.oxono.model;

import be.esi.dev.oxono.observer.Observer;
import be.esi.dev.oxono.util.Color;
import be.esi.dev.oxono.util.GamePhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    private GameFacade gameFacade;
    private TestObserver observer1;
    private TestObserver observer2;
    private TestObserver observer3;

    @BeforeEach
    void setUp() {
        // Arrange
        Player pinkPlayer = new Human(Color.PINK, 10);
        Player blackPlayer = new Human(Color.BLACK, 10);
        GameConfiguration config = new GameConfiguration(pinkPlayer, blackPlayer, 6);
        gameFacade = new GameFacade(config);

        observer1 = new TestObserver("Observer1");
        observer2 = new TestObserver("Observer2");
        observer3 = new TestObserver("Observer3");
    }

    @Nested
    class RegisterObserverTests {

        @Test
        void testRegisterObserver_validObserver_getsNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertTrue(observer1.wasNotified());
            assertEquals(1, observer1.getNotificationCount());
        }

        @Test
        void testRegisterObserver_multipleObservers_allGetNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.registerObserver(observer2);
            gameFacade.registerObserver(observer3);

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertTrue(observer1.wasNotified());
            assertTrue(observer2.wasNotified());
            assertTrue(observer3.wasNotified());
            assertEquals(1, observer1.getNotificationCount());
            assertEquals(1, observer2.getNotificationCount());
            assertEquals(1, observer3.getNotificationCount());
        }

        @Test
        void testRegisterObserver_sameObserverTwice_onlyAddedOnce() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.registerObserver(observer1); // Same observer

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertTrue(observer1.wasNotified());
            assertEquals(1, observer1.getNotificationCount());
        }

        @Test
        void testRegisterObserver_nullObserver_notAdded() {
            // Arrange & Act
            gameFacade.registerObserver(null);
            gameFacade.registerObserver(observer1);
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertTrue(observer1.wasNotified());
            assertEquals(1, observer1.getNotificationCount());
        }
    }

    @Nested
    class RemoveObserverTests {

        @Test
        void testRemoveObserver_registeredObserver_noLongerNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.registerObserver(observer2);

            // Act
            gameFacade.removeObserver(observer1);
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertFalse(observer1.wasNotified());
            assertTrue(observer2.wasNotified());
            assertEquals(0, observer1.getNotificationCount());
            assertEquals(1, observer2.getNotificationCount());
        }

        @Test
        void testRemoveObserver_notRegisteredObserver_noError() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act & Assert (must not throw an exception)
            assertDoesNotThrow(() -> gameFacade.removeObserver(observer2));

            gameFacade.handlePlayerAction(new Position(2, 2));
            assertTrue(observer1.wasNotified());
        }

        @Test
        void testRemoveObserver_nullObserver_noError() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act & Assert
            assertDoesNotThrow(() -> gameFacade.removeObserver(null));

            gameFacade.handlePlayerAction(new Position(2, 2));
            assertTrue(observer1.wasNotified());
        }

        @Test
        void testRemoveObserver_allObservers_noneNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.registerObserver(observer2);

            // Act
            gameFacade.removeObserver(observer1);
            gameFacade.removeObserver(observer2);
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertFalse(observer1.wasNotified());
            assertFalse(observer2.wasNotified());
        }
    }

    @Nested
    class NotificationTests {

        @Test
        void testNotification_afterPlayerAction_observersNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertTrue(observer1.wasNotified());
        }

        @Test
        void testNotification_afterUndo_observersNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.handlePlayerAction(new Position(2, 2));
            observer1.reset();

            // Act
            gameFacade.undo();

            // Assert
            assertTrue(observer1.wasNotified());
        }

        @Test
        void testNotification_afterRedo_observersNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.handlePlayerAction(new Position(2, 2));
            gameFacade.undo();
            observer1.reset();

            // Act
            gameFacade.redo();

            // Assert
            assertTrue(observer1.wasNotified());
        }

        @Test
        void testNotification_afterSurrender_observersNotified() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act
            gameFacade.surrender();

            // Assert
            assertTrue(observer1.wasNotified());
            assertEquals(GamePhase.GAME_OVER, gameFacade.getGamePhase());
        }

        @Test
        void testNotification_multipleActions_multipleNotifications() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2)); // Notification 1
            gameFacade.handlePlayerAction(new Position(1, 2)); // Notification 2
            gameFacade.handlePlayerAction(new Position(0, 2)); // Notification 3

            // Assert
            assertEquals(3, observer1.getNotificationCount());
        }
    }

    @Nested
    class ErrorHandlingTests {

        @Test
        void testNotification_observerThrowsException_otherObserversStillNotified() {
            // Arrange
            TestObserver faultyObserver = new TestObserver("Faulty") {
                @Override
                public void update() {
                    super.update();
                    throw new RuntimeException("Observer error");
                }
            };

            gameFacade.registerObserver(faultyObserver);
            gameFacade.registerObserver(observer1);
            gameFacade.registerObserver(observer2);

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2));

            // Assert
            assertTrue(faultyObserver.wasNotified());
            assertTrue(observer1.wasNotified());
            assertTrue(observer2.wasNotified());
        }

        @Test
        void testNotification_invalidAction_noNotification() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                gameFacade.handlePlayerAction(new Position(0, 0)); // Invalid position
            });

            assertFalse(observer1.wasNotified());
        }
    }

    @Nested
    class IntegrationTests {

        @Test
        void testObserver_registeredDuringGame_receivesUpdates() {
            // Arrange
            gameFacade.handlePlayerAction(new Position(2, 2));
            gameFacade.registerObserver(observer1); // Recorded after action

            // Act
            gameFacade.handlePlayerAction(new Position(1, 2));

            // Assert
            assertTrue(observer1.wasNotified());
        }

        @Test
        void testObserver_removedDuringGame_stopsReceivingUpdates() {
            // Arrange
            gameFacade.registerObserver(observer1);
            gameFacade.handlePlayerAction(new Position(2, 2));
            observer1.reset();

            gameFacade.removeObserver(observer1);

            // Act
            gameFacade.handlePlayerAction(new Position(1, 2));

            // Assert
            assertFalse(observer1.wasNotified());
        }

        @Test
        void testObserver_throughoutCompleteGameFlow_receivesAllUpdates() {
            // Arrange
            gameFacade.registerObserver(observer1);

            // Act
            gameFacade.handlePlayerAction(new Position(2, 2)); // Choose
            gameFacade.handlePlayerAction(new Position(1, 2)); // Move
            gameFacade.handlePlayerAction(new Position(0, 2)); // Place
            gameFacade.undo(); // Undo
            gameFacade.redo(); // Redo
            gameFacade.surrender(); // Surrender

            // Assert
            assertEquals(6, observer1.getNotificationCount());
        }
    }

    // Utility class for testing observers
    private static class TestObserver implements Observer {
        private boolean notified = false;
        private int notificationCount = 0;
        private final String name;

        public TestObserver(String name) {
            this.name = name;
        }

        @Override
        public void update() {
            notified = true;
            notificationCount++;
        }

        public boolean wasNotified() {
            return notified;
        }

        public int getNotificationCount() {
            return notificationCount;
        }

        public void reset() {
            notified = false;
            notificationCount = 0;
        }
    }
}