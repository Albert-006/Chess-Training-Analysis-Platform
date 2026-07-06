package analysis;

import engine.Board;
import model.Move;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class GameAnalyzer {
    private final MoveEvaluator moveEvaluator;

    public GameAnalyzer(int searchDepth) {
        this.moveEvaluator = new MoveEvaluator(searchDepth);
    }

    public List<MoveEvaluation> analyze(List<Move> moves) {
        Board board = new Board();
        List<MoveEvaluation> evaluations = new ArrayList<>();
        int moveNumber = 1;
        for (Move move : moves) {
            MoveEvaluation evaluation = moveEvaluator.evaluate(board, move, moveNumber);
            evaluations.add(evaluation);
            board.makeMove(move);
            moveNumber++;
        }
        return evaluations;
    }

    public List<MoveEvaluation> analyzeCoordinateMoves(List<String> coordinateMoves) {
        List<Move> moves = new ArrayList<>();
        for (String coordinateMove : coordinateMoves) {
            moves.add(parseCoordinateMove(coordinateMove));
        }
        return analyze(moves);
    }

    public double estimateAccuracy(List<MoveEvaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return 100.0;
        }
        double penalty = 0.0;
        for (MoveEvaluation evaluation : evaluations) {
            penalty += Math.min(4.0, evaluation.getEvaluationLoss()) * 8.0;
        }
        return Math.max(0.0, 100.0 - penalty / evaluations.size());
    }

    private Move parseCoordinateMove(String coordinateMove) {
        Position from = Position.fromAlgebraic(coordinateMove.substring(0, 2));
        Position to = Position.fromAlgebraic(coordinateMove.substring(2, 4));
        char promotion = coordinateMove.length() > 4 ? coordinateMove.charAt(4) : '\0';
        return new Move(from, to, promotion);
    }
}
