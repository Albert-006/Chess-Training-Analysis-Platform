package ai;

import engine.Board;
import model.Move;
import model.PieceColor;

import java.util.List;
import java.util.Optional;

public class MinimaxAI implements ChessAI {
    protected static final int CHECKMATE_SCORE = 100_000;

    protected final int depth;
    protected final EvaluationFunction evaluator;

    public MinimaxAI(int depth) {
        this(depth, new EvaluationFunction());
    }

    public MinimaxAI(int depth, EvaluationFunction evaluator) {
        this.depth = Math.max(1, depth);
        this.evaluator = evaluator;
    }

    @Override
    public Optional<Move> chooseMove(Board board, PieceColor color) {
        List<Move> legalMoves = board.generateLegalMoves(color);
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (Move move : legalMoves) {
            Board child = board.copy();
            child.makeMove(move);
            int score = minimax(child, depth - 1, color);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return Optional.ofNullable(bestMove);
    }

    protected int minimax(Board board, int remainingDepth, PieceColor rootColor) {
        if (remainingDepth == 0 || board.getGameStatus() != engine.GameStatus.ACTIVE) {
            return terminalAwareEvaluation(board, rootColor, remainingDepth);
        }

        PieceColor side = board.getSideToMove();
        List<Move> moves = board.generateLegalMoves(side);
        if (moves.isEmpty()) {
            return terminalAwareEvaluation(board, rootColor, remainingDepth);
        }

        boolean maximizing = side == rootColor;
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : moves) {
            Board child = board.copy();
            child.makeMove(move);
            int score = minimax(child, remainingDepth - 1, rootColor);
            bestScore = maximizing ? Math.max(bestScore, score) : Math.min(bestScore, score);
        }
        return bestScore;
    }

    protected int terminalAwareEvaluation(Board board, PieceColor rootColor, int remainingDepth) {
        if (board.isCheckmate(rootColor)) {
            return -CHECKMATE_SCORE - remainingDepth;
        }
        if (board.isCheckmate(rootColor.opposite())) {
            return CHECKMATE_SCORE + remainingDepth;
        }
        return evaluator.evaluate(board, rootColor);
    }
}
