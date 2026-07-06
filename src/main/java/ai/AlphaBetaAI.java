package ai;

import engine.Board;
import model.Move;
import model.Piece;
import model.PieceColor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AlphaBetaAI extends MinimaxAI {
    public AlphaBetaAI(int depth) {
        super(depth);
    }

    public AlphaBetaAI(AIDifficulty difficulty) {
        super(difficulty.getDepth());
    }

    @Override
    public Optional<Move> chooseMove(Board board, PieceColor color) {
        List<Move> legalMoves = orderMoves(board, board.generateLegalMoves(color));
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            Board child = board.copy();
            child.makeMove(move);
            int score = alphaBeta(child, depth - 1, alpha, beta, color);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return Optional.ofNullable(bestMove);
    }

    private int alphaBeta(Board board, int remainingDepth, int alpha, int beta, PieceColor rootColor) {
        if (remainingDepth == 0 || board.getGameStatus() != engine.GameStatus.ACTIVE) {
            return terminalAwareEvaluation(board, rootColor, remainingDepth);
        }

        PieceColor side = board.getSideToMove();
        List<Move> moves = orderMoves(board, board.generateLegalMoves(side));
        if (moves.isEmpty()) {
            return terminalAwareEvaluation(board, rootColor, remainingDepth);
        }

        if (side == rootColor) {
            int value = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board child = board.copy();
                child.makeMove(move);
                value = Math.max(value, alphaBeta(child, remainingDepth - 1, alpha, beta, rootColor));
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }

        int value = Integer.MAX_VALUE;
        for (Move move : moves) {
            Board child = board.copy();
            child.makeMove(move);
            value = Math.min(value, alphaBeta(child, remainingDepth - 1, alpha, beta, rootColor));
            beta = Math.min(beta, value);
            if (alpha >= beta) {
                break;
            }
        }
        return value;
    }

    private List<Move> orderMoves(Board board, List<Move> moves) {
        moves.sort(Comparator.comparingInt((Move move) -> moveScore(board, move)).reversed());
        return moves;
    }

    private int moveScore(Board board, Move move) {
        Piece target = board.getPiece(move.getTo());
        int score = target == null ? 0 : target.getValue();
        if (move.getPromotion() != '\0') {
            score += 800;
        }
        if (move.isCastling()) {
            score += 40;
        }
        return score;
    }
}
