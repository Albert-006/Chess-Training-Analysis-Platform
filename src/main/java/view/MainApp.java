package view;

import ai.AIDifficulty;
import analysis.GameAnalyzer;
import analysis.MoveEvaluation;
import controller.GameController;
import engine.Board;
import model.Move;
import model.Position;
import model.User;
import opening.Opening;
import puzzle.Puzzle;
import puzzle.PuzzleSession;
import storage.SavedGame;
import storage.UserSettings;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private Stage primaryStage;
    private Scene mainScene;
    private GameController controller;
    private BorderPane mainLayout;

    // View components for active screens
    private BoardView gameBoardView;
    private StackPane gameBoardContainer;
    private ListView<String> moveList;
    private Label statusLabel;
    private Label openingLabel;
    private Label materialLabel;
    private ProgressBar evaluationBar;

    // Puzzle Mode State
    private PuzzleSession currentPuzzleSession;
    private BoardView puzzleBoardView;
    private Label puzzleFeedbackLabel;
    private Button puzzleHintButton;
    private int puzzleHintStage = 0; // 0 = no hint, 1 = source square, 2 = full move

    // Navigation tracking
    private String currentScreen = "game";
    private VBox navigationBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        controller = new GameController(Path.of("data"));

        mainLayout = new BorderPane();
        mainScene = new Scene(mainLayout, 1220, 800);
        mainScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        applyTheme();

        stage.setTitle("Chess Training & Analysis Platform");
        stage.setScene(mainScene);
        stage.setMinWidth(960);
        stage.setMinHeight(680);

        // Enforce login first!
        showLoginScreen();

        stage.show();
    }

    public void applyTheme() {
        UserSettings settings = controller.getCurrentSettings();
        mainLayout.getStyleClass().removeAll("theme-light", "theme-dark");
        if (settings.getTheme() == UserSettings.Theme.LIGHT) {
            mainLayout.getStyleClass().add("theme-light");
        } else {
            mainLayout.getStyleClass().add("theme-dark");
        }
    }

    // ==========================================
    // 1. Authentication Screens
    // ==========================================

    private void showLoginScreen() {
        mainLayout.setLeft(null);

        VBox card = new VBox(16);
        card.setPadding(new Insets(32));
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("panel");

        Label title = new Label("♟ Chess Training Platform");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Sign in to your account or register to begin");
        subtitle.getStyleClass().add("sub-header");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        Button loginButton = new Button("Sign In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);

        Button registerButton = new Button("Create New Account");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.getStyleClass().add("button-secondary");

        loginButton.setOnAction(e -> {
            try {
                controller.login(usernameField.getText(), passwordField.getText());
                applyTheme();
                showDashboard();
            } catch (IllegalArgumentException ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.getStyleClass().setAll("banner-error");
            }
        });

        registerButton.setOnAction(e -> {
            try {
                controller.register(usernameField.getText(), passwordField.getText());
                applyTheme();
                showDashboard();
            } catch (IllegalArgumentException ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.getStyleClass().setAll("banner-error");
            }
        });

        card.getChildren().addAll(
                title, subtitle,
                new Label("Username"), usernameField,
                new Label("Password"), passwordField,
                loginButton, registerButton,
                messageLabel
        );

        StackPane centerWrapper = new StackPane(card);
        centerWrapper.setPadding(new Insets(40));
        centerWrapper.setAlignment(Pos.CENTER);

        mainLayout.setCenter(centerWrapper);
    }

    private void showDashboard() {
        navigationBox = createNavigation();
        mainLayout.setLeft(navigationBox);
        showGameScreen();
    }

    private void handleLogout() {
        controller.logout();
        applyTheme();
        showLoginScreen();
    }

    // ==========================================
    // 2. Navigation Sidebar
    // ==========================================

    private VBox createNavigation() {
        VBox navigation = new VBox(10);
        navigation.setPadding(new Insets(20, 16, 20, 16));
        navigation.getStyleClass().add("sidebar");

        Label title = new Label("♟ Chess Trainer");
        title.getStyleClass().add("app-title");

        User user = controller.getCurrentUser();
        VBox userBadge = new VBox(4);
        userBadge.getStyleClass().add("user-badge");
        Label userLabel = new Label(user != null ? user.getUsername() : "Guest");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label eloLabel = new Label("Elo: " + (user != null ? user.getEloRating() : 1200) +
                " | Puzzle: " + (user != null ? user.getPuzzleRating() : 1000));
        eloLabel.setStyle("-fx-font-size: 11px;");
        userBadge.getChildren().addAll(userLabel, eloLabel);

        Label diffLabel = new Label("AI Difficulty");
        diffLabel.getStyleClass().add("sub-header");
        ComboBox<AIDifficulty> difficulty = new ComboBox<>(FXCollections.observableArrayList(AIDifficulty.values()));
        difficulty.setValue(controller.getDifficulty());
        difficulty.setMaxWidth(Double.MAX_VALUE);
        difficulty.setOnAction(event -> controller.setDifficulty(difficulty.getValue()));

        Button game = navButton("Play vs AI", "game", this::showGameScreen);
        Button puzzle = navButton("Puzzle Mode", "puzzle", this::showPuzzleScreen);
        Button analysis = navButton("Game Analysis", "analysis", this::showAnalysisScreen);
        Button openings = navButton("Opening Explorer", "openings", this::showOpeningScreen);
        Button history = navButton("Saved Games", "history", this::showHistoryScreen);
        Button statistics = navButton("Player Stats", "stats", this::showStatisticsScreen);
        Button settings = navButton("Settings", "settings", this::showSettingsScreen);

        Button logout = new Button("Log Out");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.getStyleClass().add("logout-button");
        logout.setOnAction(event -> handleLogout());

        VBox navButtons = new VBox(6, game, puzzle, analysis, openings, history, statistics, settings);
        VBox.setVgrow(navButtons, Priority.ALWAYS);

        navigation.getChildren().addAll(
                title, userBadge,
                diffLabel, difficulty,
                new Label(""),
                navButtons,
                logout
        );
        return navigation;
    }

    private Button navButton(String text, String screenKey, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-button");
        if (screenKey.equals(currentScreen)) {
            button.getStyleClass().add("nav-button-active");
        }
        button.setOnAction(event -> {
            currentScreen = screenKey;
            refreshNavigationHighlight();
            action.run();
        });
        return button;
    }

    private void refreshNavigationHighlight() {
        if (navigationBox == null) return;
        navigationBox = createNavigation();
        mainLayout.setLeft(navigationBox);
    }

    // ==========================================
    // 3. Play vs AI Screen
    // ==========================================

    private void showGameScreen() {
        currentScreen = "game";
        gameBoardView = new BoardView(controller.getBoard(), this::handleHumanMove, controller.getCurrentSettings());
        moveList = new ListView<>();
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        openingLabel = new Label();
        materialLabel = new Label();
        evaluationBar = new ProgressBar(0.5);
        evaluationBar.setMaxWidth(Double.MAX_VALUE);

        Button newGame = new Button("New Game");
        newGame.setOnAction(event -> {
            controller.newGame();
            gameBoardView.clearLastMove();
            gameBoardView.setBoard(controller.getBoard());
            refreshGameUi();
        });

        Button hint = new Button("💡 Hint");
        hint.getStyleClass().add("button-hint");
        hint.setOnAction(event -> controller.hint().ifPresent(move -> {
            gameBoardView.highlightMove(move);
            statusLabel.setText("Hint: " + move.coordinateNotation());
        }));

        Button undo = new Button("Undo");
        undo.getStyleClass().add("button-secondary");
        undo.setOnAction(event -> {
            controller.undoLastPly();
            gameBoardView.clearLastMove();
            refreshGameUi();
        });

        Button redo = new Button("Redo");
        redo.getStyleClass().add("button-secondary");
        redo.setOnAction(event -> {
            controller.redoLastPly();
            refreshGameUi();
        });

        Button save = new Button("Save Game");
        save.getStyleClass().add("button-success");
        save.setOnAction(event -> {
            double accuracy = estimateQuickAccuracy();
            SavedGame g = controller.saveCurrentGame(accuracy);
            statusLabel.setText("Saved game (" + g.getMoves().size() + " moves)");
            refreshNavigationHighlight(); // update rating
        });

        HBox toolBar1 = new HBox(8, newGame, hint);
        HBox toolBar2 = new HBox(8, undo, redo, save);

        VBox sidePanel = new VBox(12,
                new Label("Game Status"),
                statusLabel,
                openingLabel,
                materialLabel,
                new Label("Evaluation"),
                evaluationBar,
                toolBar1,
                toolBar2,
                new Label("Move History"),
                moveList);
        sidePanel.setPadding(new Insets(20));
        sidePanel.getStyleClass().add("panel");
        VBox.setVgrow(moveList, Priority.ALWAYS);

        gameBoardContainer = new StackPane(gameBoardView);
        gameBoardContainer.setAlignment(Pos.CENTER);

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(20));
        content.setCenter(gameBoardContainer);
        content.setRight(sidePanel);
        mainLayout.setCenter(content);
        refreshGameUi();
    }

    private void handleHumanMove(Move move) {
        boolean played = controller.playHumanMove(move);
        if (!played) {
            statusLabel.setText("Illegal move: " + move.coordinateNotation());
            gameBoardView.setInvalidSquare(move.getFrom());
        } else {
            gameBoardView.setLastMove(move);
        }
        refreshGameUi();
    }

    private void refreshGameUi() {
        if (gameBoardView != null) {
            gameBoardView.setBoard(controller.getBoard());
        }
        if (moveList != null) {
            List<String> moves = controller.getBoard().getMoveHistory()
                    .stream()
                    .map(Move::coordinateNotation)
                    .collect(Collectors.toList());
            moveList.setItems(FXCollections.observableArrayList(numberedMoves(moves)));
            if (!moves.isEmpty()) {
                moveList.scrollTo(moveList.getItems().size() - 1);
            }
        }
        if (statusLabel != null) {
            statusLabel.setText("Turn: " + controller.getBoard().getSideToMove() + " | " + controller.resultText());
        }
        if (openingLabel != null) {
            openingLabel.setText("Opening: " + controller.openingName());
        }
        if (materialLabel != null) {
            materialLabel.setText("Material: " + controller.materialSummary());
        }
        if (evaluationBar != null) {
            double eval = controller.currentEvaluationForWhite();
            evaluationBar.setProgress(Math.max(0.0, Math.min(1.0, 0.5 + eval / 12.0)));
        }
        checkGameOverOverlay();
    }

    private void checkGameOverOverlay() {
        if (gameBoardContainer == null) return;

        // Remove any existing overlay
        gameBoardContainer.getChildren().removeIf(node -> node.getStyleClass().contains("overlay-card"));

        engine.GameStatus status = controller.getBoard().getGameStatus();
        if (status == engine.GameStatus.ACTIVE) {
            return;
        }

        VBox card = new VBox(14);
        card.getStyleClass().add("overlay-card");

        Label titleLabel = new Label();
        Label subtitleLabel = new Label();
        subtitleLabel.getStyleClass().add("overlay-subtitle");

        if (status == engine.GameStatus.WHITE_WON) {
            // Human is WHITE by default in controller
            titleLabel.setText("🏆 Checkmate — You Won!");
            titleLabel.getStyleClass().add("overlay-title-win");
            subtitleLabel.setText("Congratulations! White delivered checkmate.");
        } else if (status == engine.GameStatus.BLACK_WON) {
            titleLabel.setText("💀 Checkmate — You Lost!");
            titleLabel.getStyleClass().add("overlay-title-loss");
            subtitleLabel.setText("Black delivered checkmate. Better luck next time!");
        } else if (status == engine.GameStatus.DRAW_STALEMATE) {
            titleLabel.setText("⚖ Stalemate — Draw!");
            titleLabel.getStyleClass().add("overlay-title-draw");
            subtitleLabel.setText("No legal moves available. The game is a draw.");
        }

        Button playAgain = new Button("Play Again");
        playAgain.getStyleClass().add("button");
        playAgain.setOnAction(e -> {
            controller.newGame();
            if (gameBoardView != null) {
                gameBoardView.clearLastMove();
                gameBoardView.setBoard(controller.getBoard());
            }
            refreshGameUi();
        });

        Button reviewGame = new Button("Review Board");
        reviewGame.getStyleClass().add("button-secondary");
        reviewGame.setOnAction(e -> {
            gameBoardContainer.getChildren().remove(card);
        });

        HBox buttonBox = new HBox(12, playAgain, reviewGame);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleLabel, subtitleLabel, buttonBox);
        gameBoardContainer.getChildren().add(card);
    }

    // ==========================================
    // 4. Puzzle Mode Screen (Full Fix + Hint)
    // ==========================================

    private void showPuzzleScreen() {
        currentScreen = "puzzle";
        Puzzle puzzle = controller.randomPuzzle();
        currentPuzzleSession = new PuzzleSession(puzzle);
        puzzleHintStage = 0;

        puzzleBoardView = new BoardView(
                currentPuzzleSession.getBoard(),
                this::handlePuzzleMove,
                controller.getCurrentSettings()
        );

        Label titleLabel = new Label(puzzle.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Theme: " + puzzle.getMotif() + "  |  Rating: " + puzzle.getRating());
        infoLabel.getStyleClass().add("sub-header");

        puzzleFeedbackLabel = new Label("Find the best move for " + currentPuzzleSession.getBoard().getSideToMove());
        puzzleFeedbackLabel.getStyleClass().add("banner-info");
        puzzleFeedbackLabel.setWrapText(true);

        puzzleHintButton = new Button("💡 Hint");
        puzzleHintButton.getStyleClass().add("button-hint");
        puzzleHintButton.setOnAction(e -> handlePuzzleHint());

        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add("button-secondary");
        resetButton.setOnAction(e -> {
            currentPuzzleSession.reset();
            puzzleHintStage = 0;
            puzzleBoardView.clearSelection();
            puzzleBoardView.clearLastMove();
            puzzleBoardView.setBoard(currentPuzzleSession.getBoard());
            puzzleFeedbackLabel.setText("Puzzle reset. Find the best move for " + currentPuzzleSession.getBoard().getSideToMove());
            puzzleFeedbackLabel.getStyleClass().setAll("banner-info");
            puzzleHintButton.setDisable(false);
        });

        Button nextButton = new Button("Next Puzzle ➔");
        nextButton.getStyleClass().add("button-success");
        nextButton.setOnAction(e -> showPuzzleScreen());

        HBox actionButtons = new HBox(10, puzzleHintButton, resetButton, nextButton);

        VBox sidePanel = new VBox(16,
                titleLabel,
                infoLabel,
                puzzleFeedbackLabel,
                actionButtons
        );
        sidePanel.setPadding(new Insets(24));
        sidePanel.getStyleClass().add("panel");
        sidePanel.setPrefWidth(340);

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(20));
        content.setCenter(puzzleBoardView);
        content.setRight(sidePanel);
        mainLayout.setCenter(content);
    }

    private void handlePuzzleMove(Move move) {
        if (currentPuzzleSession == null || currentPuzzleSession.isCompleted()) {
            return;
        }

        PuzzleSession.MoveResult result = currentPuzzleSession.playMove(move);
        puzzleBoardView.setLastMove(move);
        puzzleBoardView.setBoard(currentPuzzleSession.getBoard());

        switch (result) {
            case CORRECT_AND_ADVANCED:
                puzzleHintStage = 0;
                puzzleFeedbackLabel.setText("Good move! Keep going...");
                puzzleFeedbackLabel.getStyleClass().setAll("banner-info");
                break;
            case CORRECT_AND_COMPLETED:
                puzzleHintStage = 0;
                puzzleFeedbackLabel.setText("🎉 Puzzle Solved! Excellent work!");
                puzzleFeedbackLabel.getStyleClass().setAll("banner-success");
                puzzleHintButton.setDisable(true);
                controller.recordPuzzleSolved(currentPuzzleSession.getPuzzle());
                refreshNavigationHighlight();
                break;
            case INCORRECT:
                puzzleFeedbackLabel.setText("❌ That's not the right move. Try again!");
                puzzleFeedbackLabel.getStyleClass().setAll("banner-warning");
                puzzleBoardView.setInvalidSquare(move.getFrom());
                controller.recordPuzzleFailed(currentPuzzleSession.getPuzzle());
                refreshNavigationHighlight();
                break;
            case ILLEGAL:
                puzzleFeedbackLabel.setText("⚠️ Illegal chess move for " + move.coordinateNotation());
                puzzleFeedbackLabel.getStyleClass().setAll("banner-error");
                puzzleBoardView.setInvalidSquare(move.getFrom());
                break;
        }
    }

    private void handlePuzzleHint() {
        if (currentPuzzleSession == null || currentPuzzleSession.isCompleted()) {
            return;
        }

        Optional<Move> nextMoveOpt = controller.getPuzzleHint(currentPuzzleSession);
        if (nextMoveOpt.isEmpty()) {
            puzzleFeedbackLabel.setText("No hint available.");
            return;
        }

        Move nextMove = nextMoveOpt.get();
        if (puzzleHintStage == 0) {
            // Reveal piece/square to move
            puzzleBoardView.setHintSquares(List.of(nextMove.getFrom()));
            puzzleFeedbackLabel.setText("Hint: Look closely at piece on " + nextMove.getFrom().toAlgebraic());
            puzzleFeedbackLabel.getStyleClass().setAll("banner-info");
            puzzleHintStage = 1;
        } else {
            // Reveal destination as well
            puzzleBoardView.setHintSquares(List.of(nextMove.getFrom(), nextMove.getTo()));
            puzzleFeedbackLabel.setText("Hint: Play move " + nextMove.getFrom().toAlgebraic() + " → " + nextMove.getTo().toAlgebraic());
            puzzleFeedbackLabel.getStyleClass().setAll("banner-info");
            puzzleHintStage = 0;
        }
    }

    // ==========================================
    // 5. Settings Screen
    // ==========================================

    private void showSettingsScreen() {
        currentScreen = "settings";
        UserSettings settings = controller.getCurrentSettings();

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setMaxWidth(600);
        content.getStyleClass().add("panel");

        Label heading = new Label("Application Settings");
        heading.getStyleClass().add("section-header");

        // Appearance Section
        Label appearanceLabel = new Label("Appearance");
        appearanceLabel.getStyleClass().add("sub-header");

        ComboBox<UserSettings.Theme> themeCombo = new ComboBox<>(FXCollections.observableArrayList(UserSettings.Theme.values()));
        themeCombo.setValue(settings.getTheme());
        themeCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<UserSettings.BoardTheme> boardThemeCombo = new ComboBox<>(FXCollections.observableArrayList(UserSettings.BoardTheme.values()));
        boardThemeCombo.setValue(settings.getBoardTheme());
        boardThemeCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<UserSettings.PieceStyle> pieceStyleCombo = new ComboBox<>(FXCollections.observableArrayList(UserSettings.PieceStyle.values()));
        pieceStyleCombo.setValue(settings.getPieceStyle());
        pieceStyleCombo.setMaxWidth(Double.MAX_VALUE);

        // Gameplay Section
        Label gameplayLabel = new Label("Gameplay & Board Visuals");
        gameplayLabel.getStyleClass().add("sub-header");

        CheckBox legalMovesCheck = new CheckBox("Highlight legal target squares");
        legalMovesCheck.setSelected(settings.isHighlightLegalMoves());

        CheckBox lastMoveCheck = new CheckBox("Highlight last move played");
        lastMoveCheck.setSelected(settings.isHighlightLastMove());

        CheckBox coordsCheck = new CheckBox("Display board coordinates (a-h, 1-8)");
        coordsCheck.setSelected(settings.isShowCoordinates());

        Label savedStatus = new Label();
        savedStatus.getStyleClass().add("banner-success");
        savedStatus.setVisible(false);

        Button saveButton = new Button("Save Preferences");
        saveButton.setOnAction(e -> {
            settings.setTheme(themeCombo.getValue());
            settings.setBoardTheme(boardThemeCombo.getValue());
            settings.setPieceStyle(pieceStyleCombo.getValue());
            settings.setHighlightLegalMoves(legalMovesCheck.isSelected());
            settings.setHighlightLastMove(lastMoveCheck.isSelected());
            settings.setShowCoordinates(coordsCheck.isSelected());

            controller.saveCurrentSettings(settings);
            applyTheme();

            if (gameBoardView != null) {
                gameBoardView.setSettings(settings);
            }
            if (puzzleBoardView != null) {
                puzzleBoardView.setSettings(settings);
            }

            savedStatus.setText("Preferences saved and applied successfully!");
            savedStatus.setVisible(true);
        });

        content.getChildren().addAll(
                heading,
                appearanceLabel,
                new Label("Color Theme:"), themeCombo,
                new Label("Chessboard Pattern:"), boardThemeCombo,
                new Label("Chess Piece Style:"), pieceStyleCombo,
                gameplayLabel,
                legalMovesCheck,
                lastMoveCheck,
                coordsCheck,
                saveButton,
                savedStatus
        );

        StackPane centerWrapper = new StackPane(content);
        centerWrapper.setPadding(new Insets(30));
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        mainLayout.setCenter(centerWrapper);
    }

    // ==========================================
    // 6. Analysis Screen
    // ==========================================

    private void showAnalysisScreen() {
        currentScreen = "analysis";
        TableView<MoveEvaluation> analysisTable = createAnalysisTable();
        LineChart<Number, Number> evaluationChart = createEvaluationChart();

        Button analyze = new Button("Analyze Current Game");
        analyze.setOnAction(event -> {
            List<MoveEvaluation> evaluations = controller.analyzeCurrentGame();
            analysisTable.setItems(FXCollections.observableArrayList(evaluations));
            fillEvaluationChart(evaluationChart, evaluations);
        });

        VBox content = new VBox(16, analyze, analysisTable, evaluationChart);
        content.setPadding(new Insets(24));
        mainLayout.setCenter(content);
    }

    private TableView<MoveEvaluation> createAnalysisTable() {
        TableView<MoveEvaluation> table = new TableView<>();
        TableColumn<MoveEvaluation, String> move = new TableColumn<>("Move");
        move.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMoveNumber() + ". " + data.getValue().getPlayedMove().coordinateNotation()));
        TableColumn<MoveEvaluation, String> best = new TableColumn<>("Best");
        best.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBestMove() == null ? "-" : data.getValue().getBestMove().coordinateNotation()));
        TableColumn<MoveEvaluation, Number> loss = new TableColumn<>("Loss (Pawns)");
        loss.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getEvaluationLoss()));
        TableColumn<MoveEvaluation, String> type = new TableColumn<>("Classification");
        type.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClassification().name()));
        table.getColumns().add(move);
        table.getColumns().add(best);
        table.getColumns().add(loss);
        table.getColumns().add(type);
        table.setMinHeight(260);
        return table;
    }

    private LineChart<Number, Number> createEvaluationChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Move Number");
        yAxis.setLabel("Centipawn / Pawn Advantage");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Game Evaluation Graph");
        chart.setLegendVisible(false);
        chart.setMinHeight(260);
        return chart;
    }

    private void fillEvaluationChart(LineChart<Number, Number> chart, List<MoveEvaluation> evaluations) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (MoveEvaluation evaluation : evaluations) {
            series.getData().add(new XYChart.Data<>(evaluation.getMoveNumber(), evaluation.getPlayedEvaluation()));
        }
        chart.getData().setAll(series);
    }

    // ==========================================
    // 7. Opening Screen
    // ==========================================

    private void showOpeningScreen() {
        currentScreen = "openings";
        ListView<Opening> openings = new ListView<>(FXCollections.observableArrayList(controller.openings()));
        Label details = new Label();
        details.setWrapText(true);
        details.getStyleClass().add("panel");
        details.setPadding(new Insets(16));

        openings.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, opening) -> {
            if (opening != null) {
                details.setText("📌 " + opening.getName() + "\n\nMain Line:\n" + opening.moveLine() + "\n\nStrategic Idea:\n" + opening.getIdea());
            }
        });
        if (!controller.openings().isEmpty()) {
            openings.getSelectionModel().selectFirst();
        }

        VBox content = new VBox(14, new Label("Opening Explorer"), openings, details);
        content.setPadding(new Insets(24));
        mainLayout.setCenter(content);
    }

    // ==========================================
    // 8. Saved Games & Replay Screen
    // ==========================================

    private void showHistoryScreen() {
        currentScreen = "history";
        ListView<SavedGame> games = new ListView<>(FXCollections.observableArrayList(controller.savedGames()));
        Board replayBoard = new Board();
        BoardView replayView = new BoardView(replayBoard, move -> {}, controller.getCurrentSettings());
        Label replayStatus = new Label("Select a saved game to replay moves");
        replayStatus.setStyle("-fx-font-weight: bold;");

        final int[] replayIndex = {0};
        final List<String>[] selectedMoves = new List[]{new ArrayList<>()};

        games.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, game) -> {
            if (game == null) {
                return;
            }
            replayBoard.setupStandardBoard();
            replayIndex[0] = 0;
            selectedMoves[0] = game.getMoves();
            replayView.clearLastMove();
            replayView.setBoard(replayBoard);
            replayStatus.setText(game.toString());
        });

        Button previous = new Button("◀ Previous");
        previous.getStyleClass().add("button-secondary");
        previous.setOnAction(event -> {
            if (replayIndex[0] > 0) {
                replayBoard.undo();
                replayIndex[0]--;
                replayView.clearLastMove();
                replayView.setBoard(replayBoard);
            }
        });

        Button next = new Button("Next ▶");
        next.getStyleClass().add("button-secondary");
        next.setOnAction(event -> {
            if (replayIndex[0] < selectedMoves[0].size()) {
                Move m = parseCoordinateMove(selectedMoves[0].get(replayIndex[0]));
                replayBoard.makeMove(m);
                replayIndex[0]++;
                replayView.setLastMove(m);
                replayView.setBoard(replayBoard);
            }
        });

        VBox side = new VBox(14, replayStatus, games, new HBox(8, previous, next));
        side.setPadding(new Insets(20));
        side.getStyleClass().add("panel");
        VBox.setVgrow(games, Priority.ALWAYS);

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(20));
        content.setCenter(replayView);
        content.setRight(side);
        mainLayout.setCenter(content);
    }

    private Move parseCoordinateMove(String coordinateMove) {
        Position from = Position.fromAlgebraic(coordinateMove.substring(0, 2));
        Position to = Position.fromAlgebraic(coordinateMove.substring(2, 4));
        char promotion = coordinateMove.length() > 4 ? coordinateMove.charAt(4) : '\0';
        return new Move(from, to, promotion);
    }

    // ==========================================
    // 9. Statistics Screen
    // ==========================================

    private void showStatisticsScreen() {
        currentScreen = "stats";
        User user = controller.getCurrentUser();
        GridPane stats = new GridPane();
        stats.setHgap(16);
        stats.setVgap(14);
        stats.setPadding(new Insets(24));
        stats.getStyleClass().add("panel");

        stats.addRow(0, new Label("Player Name:"), boldLabel(user == null ? "Guest" : user.getUsername()));
        stats.addRow(1, new Label("Standard Elo Rating:"), boldLabel(user == null ? "1200" : String.valueOf(user.getEloRating())));
        stats.addRow(2, new Label("Puzzle Rating:"), boldLabel(user == null ? "1000" : String.valueOf(user.getPuzzleRating())));
        stats.addRow(3, new Label("Total Games Played:"), boldLabel(user == null ? "0" : String.valueOf(user.getGamesPlayed())));
        stats.addRow(4, new Label("Wins:"), boldLabel(user == null ? "0" : String.valueOf(user.getWins())));
        stats.addRow(5, new Label("Losses:"), boldLabel(user == null ? "0" : String.valueOf(user.getLosses())));
        stats.addRow(6, new Label("Draws:"), boldLabel(user == null ? "0" : String.valueOf(user.getDraws())));
        stats.addRow(7, new Label("Favorite Opening:"), boldLabel(user == null ? "-" : user.getFavoriteOpening()));

        VBox content = new VBox(16, new Label("Player Profile & Statistics"), stats);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.TOP_LEFT);
        mainLayout.setCenter(content);
    }

    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private List<String> numberedMoves(List<String> moves) {
        List<String> numbered = new ArrayList<>();
        for (int i = 0; i < moves.size(); i += 2) {
            String white = moves.get(i);
            String black = i + 1 < moves.size() ? " " + moves.get(i + 1) : "";
            numbered.add((i / 2 + 1) + ". " + white + black);
        }
        return numbered;
    }

    private double estimateQuickAccuracy() {
        GameAnalyzer analyzer = new GameAnalyzer(1);
        List<MoveEvaluation> evaluations = analyzer.analyze(new ArrayList<>(controller.getBoard().getMoveHistory()));
        return analyzer.estimateAccuracy(evaluations);
    }
}
