package model;

import engine.Board;

import java.util.List;

public class Queen extends Piece {
    private static final long serialVersionUID = 1L;

    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    public Queen(PieceColor color, Position position) {
        super(color, position, 900);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board) {
        return slidingMoves(board, DIRECTIONS);
    }

    @Override
    public Piece copy() {
        return copyState(new Queen(getColor(), getPosition()));
    }

    @Override
    public char getFenSymbol() {
        return getColor() == PieceColor.WHITE ? 'Q' : 'q';
    }

    @Override
    public String getDisplaySymbol() {
        return getColor() == PieceColor.WHITE ? "\u2655" : "\u265B";
    }
}
