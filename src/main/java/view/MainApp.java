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
import puzzle.PuzzleValidator;
import storage.SavedGame;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private GameController controller;
    private BorderPane root;
    private BoardView boardView;
    private ListView<String> moveList;
    private Label statusLabel;
    private Label openingLabel;
    private Label materialLabel;
    private ProgressBar evaluationBar;
    private TableView<MoveEvaluation> analysisTable;
    private LineChart<Number, Number> evaluationChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        controller = new GameController(Path.of("data"));
        root = new BorderPane();
        root.setLeft(createNavigation());
        showGameScreen();

        Scene scene = new Scene(root, 1180, 780);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Chess Training & Analysis Platform");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createNavigation() {
        VBox navigation = new VBox(10);
        navigation.setPadding(new Insets(18));
        navigation.getStyleClass().add("sidebar");

        Label title = new Label("Chess Trainer");
        title.getStyleClass().add("app-title");

        TextField username = new TextField("student");
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setText("password");
        Button login = new Button("Login / Register");
        Label userInfo = new Label("Guest");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> {
            try {
                User user = controller.loginOrRegister(username.getText(), password.getText());
                userInfo.setText(user.getUsername() + " | Elo " + user.getEloRating());
            } catch (IllegalArgumentException exception) {
                userInfo.setText(exception.getMessage());
            }
        });

        ComboBox<AIDifficulty> difficulty = new ComboBox<>(FXCollections.observableArrayList(AIDifficulty.values()));
        difficulty.setValue(controller.getDifficulty());
        difficulty.setMaxWidth(Double.MAX_VALUE);
        difficulty.setOnAction(event -> controller.setDifficulty(difficulty.getValue()));

        Button game = navButton("Play vs AI", this::showGameScreen);
        Button puzzle = navButton("Puzzle Mode", this::showPuzzleScreen);
        Button analysis = navButton("Analysis", this::showAnalysisScreen);
        Button openings = navButton("Opening Explorer", this::showOpeningScreen);
        Button history = navButton("Game History", this::showHistoryScreen);
        Button statistics = navButton("Statistics", this::showStatisticsScreen);

        navigation.getChildren().addAll(
                title, username, password, login, userInfo,
                new Label("AI Difficulty"), difficulty,
                game, puzzle, analysis, openings, history, statistics);
        return navigation;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showGameScreen() {
        boardView = new BoardView(controller.getBoard(), this::handleHumanMove);
        moveList = new ListView<>();
        statusLabel = new Label();
        openingLabel = new Label();
        materialLabel = new Label();
        evaluationBar = new ProgressBar(0.5);
        evaluationBar.setMaxWidth(Double.MAX_VALUE);

        Button newGame = new Button("New Game");
        newGame.setOnAction(event -> {
            controller.newGame();
            boardView.setBoard(controller.getBoard());
            refreshGameUi();
        });
        Button hint = new Button("Hint");
        hint.setOnAction(event -> controller.hint().ifPresent(move -> {
            boardView.highlightMove(move);
            statusLabel.setText("Hint: " + move.coordinateNotation());
        }));
        Button undo = new Button("Undo");
        undo.setOnAction(event -> {
            controller.undoLastPly();
            refreshGameUi();
        });
        Button redo = new Button("Redo");
        redo.setOnAction(event -> {
            controller.redoLastPly();
            refreshGameUi();
        });
        Button save = new Button("Save Game");
        save.setOnAction(event -> {
            double accuracy = estimateQuickAccuracy();
            SavedGame game = controller.saveCurrentGame(accuracy);
            statusLabel.setText("Saved " + game.getId());
        });

        VBox sidePanel = new VBox(12,
                statusLabel,
                openingLabel,
                materialLabel,
                new Label("Evaluation"),
                evaluationBar,
                new HBox(8, newGame, hint),
                new HBox(8, undo, redo, save),
                new Label("Moves"),
                moveList);
        sidePanel.setPadding(new Insets(18));
        sidePanel.getStyleClass().add("panel");
        VBox.setVgrow(moveList, Priority.ALWAYS);

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(18));
        content.setCenter(boardView);
        content.setRight(sidePanel);
        root.setCenter(content);
        refreshGameUi();
    }

    private void handleHumanMove(Move move) {
        boolean played = controller.playHumanMove(move);
        if (!played) {
            statusLabel.setText("Illegal move: " + move.coordinateNotation());
        }
        refreshGameUi();
    }

    private void refreshGameUi() {
        if (boardView != null) {
            boardView.setBoard(controller.getBoard());
        }
        if (moveList != null) {
            List<String> moves = controller.getBoard().getMoveHistory()
                    .stream()
                    .map(Move::coordinateNotation)
                    .collect(Collectors.toList());
            moveList.setItems(FXCollections.observableArrayList(numberedMoves(moves)));
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

    private void showPuzzleScreen() {
        Puzzle puzzle = controller.randomPuzzle();
        Board puzzleBoard = Board.fromFen(puzzle.getFen());
        PuzzleValidator validator = new PuzzleValidator();
        Label puzzleStatus = new Label(puzzle.getTitle() + " | " + puzzle.getMotif() + " | " + puzzle.getRating());
        BoardView puzzleBoardView = new BoardView(puzzleBoard, move -> {
            if (!validator.isCorrectMove(puzzle, 0, move)) {
                puzzleStatus.setText("Try again: " + puzzle.getMotif());
                return;
            }
            if (puzzleBoard.makeMove(move)) {
                puzzleStatus.setText("Solved: " + move.coordinateNotation());
            }
        });
        Button next = new Button("Random Puzzle");
        next.setOnAction(event -> showPuzzleScreen());
        VBox side = new VBox(12, puzzleStatus, next, new Label("Solution theme: " + puzzle.getMotif()));
        side.setPadding(new Insets(18));
        side.getStyleClass().add("panel");
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(18));
        content.setCenter(puzzleBoardView);
        content.setRight(side);
        root.setCenter(content);
    }

    private void showAnalysisScreen() {
        analysisTable = createAnalysisTable();
        evaluationChart = createEvaluationChart();
        Button analyze = new Button("Analyze Current Game");
        analyze.setOnAction(event -> {
            List<MoveEvaluation> evaluations = controller.analyzeCurrentGame();
            analysisTable.setItems(FXCollections.observableArrayList(evaluations));
            fillEvaluationChart(evaluations);
        });
        VBox content = new VBox(12, analyze, analysisTable, evaluationChart);
        content.setPadding(new Insets(18));
        root.setCenter(content);
    }

    private TableView<MoveEvaluation> createAnalysisTable() {
        TableView<MoveEvaluation> table = new TableView<>();
        TableColumn<MoveEvaluation, String> move = new TableColumn<>("Move");
        move.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMoveNumber() + ". " + data.getValue().getPlayedMove().coordinateNotation()));
        TableColumn<MoveEvaluation, String> best = new TableColumn<>("Best");
        best.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBestMove() == null ? "-" : data.getValue().getBestMove().coordinateNotation()));
        TableColumn<MoveEvaluation, Number> loss = new TableColumn<>("Loss");
        loss.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getEvaluationLoss()));
        TableColumn<MoveEvaluation, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClassification().name()));
        table.getColumns().add(move);
        table.getColumns().add(best);
        table.getColumns().add(loss);
        table.getColumns().add(type);
        table.setMinHeight(280);
        return table;
    }

    private LineChart<Number, Number> createEvaluationChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Move");
        yAxis.setLabel("Evaluation");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evaluation Graph");
        chart.setLegendVisible(false);
        chart.setMinHeight(260);
        return chart;
    }

    private void fillEvaluationChart(List<MoveEvaluation> evaluations) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (MoveEvaluation evaluation : evaluations) {
            series.getData().add(new XYChart.Data<>(evaluation.getMoveNumber(), evaluation.getPlayedEvaluation()));
        }
        evaluationChart.getData().setAll(series);
    }

    private void showOpeningScreen() {
        ListView<Opening> openings = new ListView<>(FXCollections.observableArrayList(controller.openings()));
        Label details = new Label();
        details.setWrapText(true);
        openings.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, opening) -> {
            if (opening != null) {
                details.setText(opening.getName() + "\nLine: " + opening.moveLine() + "\nIdea: " + opening.getIdea());
            }
        });
        if (!controller.openings().isEmpty()) {
            openings.getSelectionModel().selectFirst();
        }
        VBox content = new VBox(12, new Label("Opening Explorer"), openings, details);
        content.setPadding(new Insets(18));
        root.setCenter(content);
    }

    private void showHistoryScreen() {
        ListView<SavedGame> games = new ListView<>(FXCollections.observableArrayList(controller.savedGames()));
        Board replayBoard = new Board();
        BoardView replayView = new BoardView(replayBoard, move -> {
        });
        Label replayStatus = new Label("Select a saved game");
        final int[] replayIndex = {0};
        final List<String>[] selectedMoves = new List[]{new ArrayList<>()};

        games.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, game) -> {
            if (game == null) {
                return;
            }
            replayBoard.setupStandardBoard();
            replayIndex[0] = 0;
            selectedMoves[0] = game.getMoves();
            replayView.setBoard(replayBoard);
            replayStatus.setText(game.toString());
        });

        Button previous = new Button("Previous");
        previous.setOnAction(event -> {
            if (replayIndex[0] > 0) {
                replayBoard.undo();
                replayIndex[0]--;
                replayView.setBoard(replayBoard);
            }
        });
        Button next = new Button("Next");
        next.setOnAction(event -> {
            if (replayIndex[0] < selectedMoves[0].size()) {
                replayBoard.makeMove(parseCoordinateMove(selectedMoves[0].get(replayIndex[0])));
                replayIndex[0]++;
                replayView.setBoard(replayBoard);
            }
        });

        VBox side = new VBox(12, replayStatus, games, new HBox(8, previous, next));
        side.setPadding(new Insets(18));
        side.getStyleClass().add("panel");
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(18));
        content.setCenter(replayView);
        content.setRight(side);
        root.setCenter(content);
    }

    private Move parseCoordinateMove(String coordinateMove) {
        Position from = Position.fromAlgebraic(coordinateMove.substring(0, 2));
        Position to = Position.fromAlgebraic(coordinateMove.substring(2, 4));
        char promotion = coordinateMove.length() > 4 ? coordinateMove.charAt(4) : '\0';
        return new Move(from, to, promotion);
    }

    private void showStatisticsScreen() {
        User user = controller.getCurrentUser();
        GridPane stats = new GridPane();
        stats.setHgap(12);
        stats.setVgap(12);
        stats.setPadding(new Insets(18));
        stats.addRow(0, new Label("User"), new Label(user == null ? "Guest" : user.getUsername()));
        stats.addRow(1, new Label("Elo"), new Label(user == null ? "1200" : String.valueOf(user.getEloRating())));
        stats.addRow(2, new Label("Games"), new Label(user == null ? "0" : String.valueOf(user.getGamesPlayed())));
        stats.addRow(3, new Label("Wins"), new Label(user == null ? "0" : String.valueOf(user.getWins())));
        stats.addRow(4, new Label("Losses"), new Label(user == null ? "0" : String.valueOf(user.getLosses())));
        stats.addRow(5, new Label("Draws"), new Label(user == null ? "0" : String.valueOf(user.getDraws())));
        stats.addRow(6, new Label("Puzzle Rating"), new Label(user == null ? "1000" : String.valueOf(user.getPuzzleRating())));
        stats.addRow(7, new Label("Favorite Opening"), new Label(user == null ? "-" : user.getFavoriteOpening()));

        VBox content = new VBox(12, new Label("Statistics"), stats);
        content.setPadding(new Insets(18));
        content.setAlignment(Pos.TOP_LEFT);
        root.setCenter(content);
    }
}
