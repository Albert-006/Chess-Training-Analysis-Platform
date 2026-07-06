package model;

import engine.Board;

import java.util.List;

public class Rook extends Piece {
    private static final long serialVersionUID = 1L;

    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    public Rook(PieceColor color, Position position) {
        super(color, position, 500);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board) {
        return slidingMoves(board, DIRECTIONS);
    }

    @Override
    public Piece copy() {
        return copyState(new Rook(getColor(), getPosition()));
    }

    @Override
    public char getFenSymbol() {
        return getColor() == PieceColor.WHITE ? 'R' : 'r';
    }

    @Override
    public String getDisplaySymbol() {
        return getColor() == PieceColor.WHITE ? "\u2656" : "\u265C";
    }
}
