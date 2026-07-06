package controller;

import ai.AIDifficulty;
import ai.AlphaBetaAI;
import ai.EvaluationFunction;
import analysis.GameAnalyzer;
import analysis.MoveEvaluation;
import engine.Board;
import engine.GameStatus;
import model.Move;
import model.Piece;
import model.PieceColor;
import model.User;
import opening.Opening;
import opening.OpeningBook;
import puzzle.Puzzle;
import puzzle.PuzzleService;
import storage.GameStorage;
import storage.SavedGame;
import storage.UserStorage;
import util.EloCalculator;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameController {
    private static final int AI_RATING = 1250;

    private final OpeningBook openingBook = new OpeningBook();
    private final PuzzleService puzzleService = new PuzzleService();
    private final UserStorage userStorage;
    private final GameStorage gameStorage;
    private final EvaluationFunction evaluationFunction = new EvaluationFunction();

    private Board board = new Board();
    private User currentUser;
    private AIDifficulty difficulty = AIDifficulty.EASY;
    private PieceColor humanColor = PieceColor.WHITE;

    public GameController(Path dataRoot) {
        this.userStorage = new UserStorage(dataRoot);
        this.gameStorage = new GameStorage(dataRoot);
    }

    public Board getBoard() {
        return board;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public AIDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(AIDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public User loginOrRegister(String username, String password) {
        Optional<User> authenticated = userStorage.authenticate(username, password);
        if (authenticated.isPresent()) {
            currentUser = authenticated.get();
            return currentUser;
        }
        if (userStorage.find(username).isPresent()) {
            throw new IllegalArgumentException("Invalid password for " + username);
        }
        currentUser = userStorage.register(username, password);
        return currentUser;
    }

    public void newGame() {
        board = new Board();
    }

    public boolean playHumanMove(Move move) {
        if (board.getSideToMove() != humanColor || board.getGameStatus() != GameStatus.ACTIVE) {
            return false;
        }
        boolean played = board.makeMove(move);
        if (played && board.getGameStatus() == GameStatus.ACTIVE) {
            playAiMove();
        }
        return played;
    }

    public Optional<Move> playAiMove() {
        AlphaBetaAI ai = new AlphaBetaAI(difficulty);
        Optional<Move> aiMove = ai.chooseMove(board, board.getSideToMove());
        aiMove.ifPresent(board::makeMove);
        return aiMove;
    }

    public Optional<Move> hint() {
        AlphaBetaAI ai = new AlphaBetaAI(Math.max(1, difficulty.getDepth()));
        return ai.chooseMove(board, board.getSideToMove());
    }

    public void undoLastPly() {
        board.undo();
    }

    public void redoLastPly() {
        board.redo();
    }

    public String openingName() {
        return openingBook.describeCurrentLine(board.getMoveHistory());
    }

    public List<Opening> openings() {
        return openingBook.getOpenings();
    }

    public Puzzle randomPuzzle() {
        return puzzleService.randomPuzzle();
    }

    public List<Puzzle> puzzles() {
        return puzzleService.getPuzzles();
    }

    public List<MoveEvaluation> analyzeCurrentGame() {
        return new GameAnalyzer(1).analyze(new ArrayList<>(board.getMoveHistory()));
    }

    public double currentEvaluationForWhite() {
        return evaluationFunction.pawns(evaluationFunction.evaluate(board, PieceColor.WHITE));
    }

    public SavedGame saveCurrentGame(double accuracy) {
        String username = currentUser == null ? "guest" : currentUser.getUsername();
        SavedGame game = new SavedGame(
                UUID.randomUUID().toString(),
                username,
                board.getMoveHistory().stream().map(Move::coordinateNotation).collect(Collectors.toList()),
                LocalDateTime.now(),
                resultText(),
                openingName(),
                accuracy,
                currentUser == null ? 1200 : currentUser.getEloRating(),
                AI_RATING);
        gameStorage.save(game);
        updateUserAfterGame();
        return game;
    }

    public List<SavedGame> savedGames() {
        return gameStorage.loadAll();
    }

    public String resultText() {
        GameStatus status = board.getGameStatus();
        if (status == GameStatus.WHITE_WON) {
            return "1-0";
        }
        if (status == GameStatus.BLACK_WON) {
            return "0-1";
        }
        if (status == GameStatus.DRAW_STALEMATE) {
            return "1/2-1/2";
        }
        return "In progress";
    }

    public String materialSummary() {
        Map<PieceColor, Integer> material = new EnumMap<>(PieceColor.class);
        material.put(PieceColor.WHITE, 0);
        material.put(PieceColor.BLACK, 0);
        for (PieceColor color : PieceColor.values()) {
            for (Piece piece : board.getPieces(color)) {
                if (piece.getValue() < 10_000) {
                    material.put(color, material.get(color) + piece.getValue());
                }
            }
        }
        int diff = material.get(PieceColor.WHITE) - material.get(PieceColor.BLACK);
        if (diff == 0) {
            return "Material equal";
        }
        return diff > 0 ? "White +" + diff : "Black +" + Math.abs(diff);
    }

    private void updateUserAfterGame() {
        if (currentUser == null || board.getGameStatus() == GameStatus.ACTIVE) {
            return;
        }
        double score;
        if (board.getGameStatus() == GameStatus.WHITE_WON) {
            score = humanColor == PieceColor.WHITE ? 1.0 : 0.0;
        } else if (board.getGameStatus() == GameStatus.BLACK_WON) {
            score = humanColor == PieceColor.BLACK ? 1.0 : 0.0;
        } else {
            score = 0.5;
        }
        currentUser.recordResult(score);
        currentUser.setEloRating(EloCalculator.updateRating(currentUser.getEloRating(), AI_RATING, score, 32));
        currentUser.setFavoriteOpening(openingName());
        userStorage.save(currentUser);
    }
}
