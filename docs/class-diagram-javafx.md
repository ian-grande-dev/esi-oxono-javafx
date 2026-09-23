## Class diagram – JavaFX layer

```mermaid
classDiagram
    direction TB

    class Application {
        <<JavaFX>>
    }

    class MainFX {
        +start(Stage primaryStage)
        +main(String[] args)$
    }

    class SetupController {
        -ComboBox player1LevelComboBox
        -ComboBox player2LevelComboBox
        -TextField boardSizeField
        -GameSetupFacade gameSetupFacade
        -handleStartGame(ActionEvent event)
        -configureGame(String p1, String p2, int size)
        -loadGameView(GameConfiguration config)
    }

    class GameController {
        -BoardController boardController
        -GameButtonsController gameButtonsController
        -PlayerInformationController playerInformationController
        -GameFacade gameFacade
        +setGameConfiguration(GameConfiguration config)
        +setMessageLabel(Label turn, Label status, Label error)
    }

    class BoardController {
        -GridPane gameGridPane
        -GameFacade gameFacade
        +update()
        +createBoard(int size)
        +setGameFacade(GameFacade facade)
        -createClickableCell(int col, int row) StackPane
        -updateBoardDisplay()
        -checkGameOver()
        -triggerBotTurnIfNeeded()
    }

    class GameButtonsController {
        -GameFacade gameFacade
        -Timeline surrenderTimer
        -onUndoButtonClick()
        -onRedoButtonClick()
        -onSurrenderButtonPressed(MouseEvent e)
        -onSurrenderButtonReleased(MouseEvent e)
        -onBackToSetupButtonClick()
        +setGameFacade(GameFacade facade)
    }

    class PlayerInformationController {
        -GameFacade gameFacade
        +update()
        +setGameFacade(GameFacade facade)
    }

    class Observer {
        <<interface>>
        +update()
    }

    class Observable {
        <<interface>>
        +registerObserver(Observer o)
        +removeObserver(Observer o)
    }

    class GameFacade {
        <<Model>>
        +handlePlayerAction(Position p)
        +handleBotAction() Position
        +undo()
        +redo()
        +surrender()
        +getBoardState() Map~Position, Pawn~
        +getValidPositions() List~Position~
        +getGamePhase() GamePhase
    }

    class GameSetupFacade {
        <<Model>>
        +createGameConfiguration() GameConfiguration
        +setBoardSize(int size)
        +setLevelPinkPlayer(int level)
        +setLevelBlackPlayer(int level)
    }

    Application <|-- MainFX
    MainFX ..> SetupController : loads setup.fxml
    SetupController --> GameSetupFacade
    SetupController ..> GameController : loads game.fxml
    GameController *-- BoardController
    GameController *-- GameButtonsController
    GameController *-- PlayerInformationController
    GameController ..> GameFacade : creates
    BoardController --> GameFacade
    GameButtonsController --> GameFacade
    PlayerInformationController --> GameFacade
    Observer <|.. BoardController
    Observer <|.. PlayerInformationController
    Observable <|.. GameFacade
    GameFacade o-- Observer : notifies
```

The view layer follows the **MVC** pattern with **FXML sub-controllers**: `GameController` composes three nested controllers (`fx:include`) and hands them a single `GameFacade`. `BoardController` and `PlayerInformationController` implement the **Observer** pattern to refresh automatically whenever the model changes.
