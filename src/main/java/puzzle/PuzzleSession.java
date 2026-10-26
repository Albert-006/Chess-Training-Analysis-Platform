package puzzle;

import engine.Board;
import model.Move;
import model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PuzzleSession {
    private final Puzzle puzzle;
    private final Board board;
    private final List<Move> solutionMoves;
    private int currentStep; // index into solutionMoves
    private boolean completed;
    private boolean failed;

    public PuzzleSession(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.board = Board.fromFen(puzzle.getFen());
        this.solutionMoves = parseMoves(puzzle.getSolutionMoves());
        this.currentStep = 0;
        this.completed = false;
        this.failed = false;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public Board getBoard() {
        return board;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public List<Move> getSolutionMoves() {
        return Collections.unmodifiableList(solutionMoves);
    }

    public void reset() {
        Board fresh = Board.fromFen(puzzle.getFen());
        // copy pieces and state
        board.clear();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position p = new Position(r, c);
                board.setPiece(p, fresh.getPiece(p));
            }
        }
        board.setSideToMove(fresh.getSideToMove());
        currentStep = 0;
        completed = false;
        failed = false;
    }

    public Optional<Move> getNextExpectedMove() {
        if (completed || currentStep >= solutionMoves.size()) {
            return Optional.empty();
        }
        return Optional.of(solutionMoves.get(currentStep));
    }

    public enum MoveResult {
        CORRECT_AND_ADVANCED,
        CORRECT_AND_COMPLETED,
        INCORRECT,
        ILLEGAL
    }

    public MoveResult playMove(Move userMove) {
        if (completed) {
            return MoveResult.ILLEGAL;
        }
        if (currentStep >= solutionMoves.size()) {
            return MoveResult.ILLEGAL;
        }

        // Must be a legal chess move on the puzzle board
        if (!board.isLegalMove(userMove)) {
            return MoveResult.ILLEGAL;
        }

        Move expected = solutionMoves.get(currentStep);
        if (!expected.sameIntent(userMove)) {
            failed = true;
            return MoveResult.INCORRECT;
        }

        // Make user move
        board.makeMove(userMove);
        currentStep++;

        if (currentStep >= solutionMoves.size()) {
            completed = true;
            return MoveResult.CORRECT_AND_COMPLETED;
        }

        // Automatically play opponent counter-move if available in the puzzle sequence
        Move counterMove = solutionMoves.get(currentStep);
        if (board.makeMove(counterMove)) {
            currentStep++;
            if (currentStep >= solutionMoves.size()) {
                completed = true;
                return MoveResult.CORRECT_AND_COMPLETED;
            }
            return MoveResult.CORRECT_AND_ADVANCED;
        } else {
            // Counter-move couldn't be played (rare defect in puzzle), mark complete
            completed = true;
            return MoveResult.CORRECT_AND_COMPLETED;
        }
    }

    private static List<Move> parseMoves(List<String> coordinateMoves) {
        List<Move> moves = new ArrayList<>();
        for (String notation : coordinateMoves) {
            if (notation.length() < 4) {
                continue;
            }
            Position from = Position.fromAlgebraic(notation.substring(0, 2));
            Position to = Position.fromAlgebraic(notation.substring(2, 4));
            char promotion = notation.length() > 4 ? notation.charAt(4) : '\0';
            moves.add(new Move(from, to, promotion));
        }
        return moves;
    }
}
