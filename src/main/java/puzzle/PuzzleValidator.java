package puzzle;

import engine.Board;
import model.Move;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class PuzzleValidator {
    public boolean isCorrectMove(Puzzle puzzle, int plyIndex, Move move) {
        if (plyIndex < 0 || plyIndex >= puzzle.getSolutionMoves().size()) {
            return false;
        }
        return puzzle.getSolutionMoves().get(plyIndex).equalsIgnoreCase(move.coordinateNotation());
    }

    public boolean validateAttempt(Puzzle puzzle, List<Move> attemptedMoves) {
        if (attemptedMoves.size() != puzzle.getSolutionMoves().size()) {
            return false;
        }
        for (int i = 0; i < attemptedMoves.size(); i++) {
            if (!isCorrectMove(puzzle, i, attemptedMoves.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean validateAndPlayLegalMoves(Puzzle puzzle, List<String> coordinateMoves) {
        Board board = Board.fromFen(puzzle.getFen());
        List<Move> moves = parseMoves(coordinateMoves);
        if (!validateAttempt(puzzle, moves)) {
            return false;
        }
        for (Move move : moves) {
            if (!board.makeMove(move)) {
                return false;
            }
        }
        return true;
    }

    private List<Move> parseMoves(List<String> coordinateMoves) {
        List<Move> moves = new ArrayList<>();
        for (String coordinateMove : coordinateMoves) {
            if (coordinateMove.length() < 4) {
                throw new IllegalArgumentException("Move must use coordinate notation: " + coordinateMove);
            }
            Position from = Position.fromAlgebraic(coordinateMove.substring(0, 2));
            Position to = Position.fromAlgebraic(coordinateMove.substring(2, 4));
            char promotion = coordinateMove.length() > 4 ? coordinateMove.charAt(4) : '\0';
            moves.add(new Move(from, to, promotion));
        }
        return moves;
    }
}
