package ai;

import engine.Board;
import model.Bishop;
import model.King;
import model.Knight;
import model.Pawn;
import model.Piece;
import model.PieceColor;
import model.Position;
import model.Queen;
import model.Rook;

import java.util.List;

public class EvaluationFunction {
    private static final int CHECKMATE_SCORE = 100_000;

    public int evaluate(Board board, PieceColor perspective) {
        if (board.isCheckmate(perspective)) {
            return -CHECKMATE_SCORE;
        }
        if (board.isCheckmate(perspective.opposite())) {
            return CHECKMATE_SCORE;
        }

        int score = 0;
        score += material(board, perspective);
        score += centerControl(board, perspective);
        score += mobility(board, perspective);
        score += kingSafety(board, perspective);
        score += pawnStructure(board, perspective);
        score += pieceActivity(board, perspective);
        return score;
    }

    private int material(Board board, PieceColor perspective) {
        int score = 0;
        for (PieceColor color : PieceColor.values()) {
            int sign = color == perspective ? 1 : -1;
            for (Piece piece : board.getPieces(color)) {
                if (!(piece instanceof King)) {
                    score += sign * piece.getValue();
                }
            }
        }
        return score;
    }

    private int centerControl(Board board, PieceColor perspective) {
        int score = 0;
        for (PieceColor color : PieceColor.values()) {
            int sign = color == perspective ? 1 : -1;
            for (Piece piece : board.getPieces(color)) {
                Position position = piece.getPosition();
                if ((position.getRow() == 3 || position.getRow() == 4)
                        && (position.getCol() == 3 || position.getCol() == 4)) {
                    score += sign * 25;
                } else if (position.getRow() >= 2 && position.getRow() <= 5
                        && position.getCol() >= 2 && position.getCol() <= 5) {
                    score += sign * 10;
                }
            }
        }
        return score;
    }

    private int mobility(Board board, PieceColor perspective) {
        int ownMoves = board.generateLegalMoves(perspective).size();
        int opponentMoves = board.generateLegalMoves(perspective.opposite()).size();
        return (ownMoves - opponentMoves) * 3;
    }

    private int kingSafety(Board board, PieceColor perspective) {
        int score = 0;
        if (board.isInCheck(perspective)) {
            score -= 60;
        }
        if (board.isInCheck(perspective.opposite())) {
            score += 60;
        }
        score += kingShelter(board, perspective);
        score -= kingShelter(board, perspective.opposite());
        return score;
    }

    private int kingShelter(Board board, PieceColor color) {
        Position king = board.findKing(color);
        if (king == null) {
            return -200;
        }
        int score = 0;
        int pawnRow = king.getRow() + color.pawnDirection();
        for (int col = king.getCol() - 1; col <= king.getCol() + 1; col++) {
            if (!Position.isValid(pawnRow, col)) {
                continue;
            }
            Piece piece = board.getPiece(new Position(pawnRow, col));
            if (piece instanceof Pawn && piece.getColor() == color) {
                score += 12;
            }
        }
        return score;
    }

    private int pawnStructure(Board board, PieceColor perspective) {
        return pawnStructureScore(board, perspective) - pawnStructureScore(board, perspective.opposite());
    }

    private int pawnStructureScore(Board board, PieceColor color) {
        int score = 0;
        int[] pawnsByFile = new int[8];
        for (Piece piece : board.getPieces(color)) {
            if (piece instanceof Pawn) {
                pawnsByFile[piece.getPosition().getCol()]++;
            }
        }
        for (int file = 0; file < 8; file++) {
            if (pawnsByFile[file] > 1) {
                score -= (pawnsByFile[file] - 1) * 18;
            }
            boolean hasNeighbor = (file > 0 && pawnsByFile[file - 1] > 0)
                    || (file < 7 && pawnsByFile[file + 1] > 0);
            if (pawnsByFile[file] > 0 && !hasNeighbor) {
                score -= 10;
            }
        }
        return score;
    }

    private int pieceActivity(Board board, PieceColor perspective) {
        int score = 0;
        for (PieceColor color : PieceColor.values()) {
            int sign = color == perspective ? 1 : -1;
            for (Piece piece : board.getPieces(color)) {
                score += sign * activityBonus(piece);
            }
        }
        return score;
    }

    private int activityBonus(Piece piece) {
        Position position = piece.getPosition();
        int advancement = piece.getColor() == PieceColor.WHITE
                ? 6 - position.getRow()
                : position.getRow() - 1;
        if (piece instanceof Knight || piece instanceof Bishop) {
            return Math.max(0, advancement) * 6;
        }
        if (piece instanceof Rook) {
            return Math.abs(3 - position.getCol()) <= 1 ? 8 : 0;
        }
        if (piece instanceof Queen) {
            return Math.max(0, advancement) * 3;
        }
        return 0;
    }

    public double pawns(int centipawns) {
        return centipawns / 100.0;
    }

    public int evaluateAfterMoves(Board board, PieceColor perspective, List<model.Move> moves) {
        Board copy = board.copy();
        for (model.Move move : moves) {
            if (!copy.makeMove(move)) {
                break;
            }
        }
        return evaluate(copy, perspective);
    }
}
