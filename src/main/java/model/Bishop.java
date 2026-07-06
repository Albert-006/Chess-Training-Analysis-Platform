package model;

import engine.Board;

import java.util.List;

public class Bishop extends Piece {
    private static final long serialVersionUID = 1L;

    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    public Bishop(PieceColor color, Position position) {
        super(color, position, 330);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board) {
        return slidingMoves(board, DIRECTIONS);
    }

    @Override
    public Piece copy() {
        return copyState(new Bishop(getColor(), getPosition()));
    }

    @Override
    public char getFenSymbol() {
        return getColor() == PieceColor.WHITE ? 'B' : 'b';
    }

    @Override
    public String getDisplaySymbol() {
        return getColor() == PieceColor.WHITE ? "\u2657" : "\u265D";
    }
}
