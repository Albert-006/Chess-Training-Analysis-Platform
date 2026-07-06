package model;

import engine.Board;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    private static final long serialVersionUID = 1L;

    private static final int[][] OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public Knight(PieceColor color, Position position) {
        super(color, position, 320);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        for (int[] offset : OFFSETS) {
            addStepMove(board, moves, offset[0], offset[1]);
        }
        return moves;
    }

    @Override
    public Piece copy() {
        return copyState(new Knight(getColor(), getPosition()));
    }

    @Override
    public char getFenSymbol() {
        return getColor() == PieceColor.WHITE ? 'N' : 'n';
    }

    @Override
    public String getDisplaySymbol() {
        return getColor() == PieceColor.WHITE ? "\u2658" : "\u265E";
    }
}
