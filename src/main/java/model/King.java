package model;

import engine.Board;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    private static final long serialVersionUID = 1L;

    public King(PieceColor color, Position position) {
        super(color, position, 20000);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        for (int rowDelta = -1; rowDelta <= 1; rowDelta++) {
            for (int colDelta = -1; colDelta <= 1; colDelta++) {
                if (rowDelta != 0 || colDelta != 0) {
                    addStepMove(board, moves, rowDelta, colDelta);
                }
            }
        }
        if (board.canCastle(getColor(), true)) {
            moves.add(Move.castle(getPosition(), new Position(getPosition().getRow(), 6)));
        }
        if (board.canCastle(getColor(), false)) {
            moves.add(Move.castle(getPosition(), new Position(getPosition().getRow(), 2)));
        }
        return moves;
    }

    @Override
    public Piece copy() {
        return copyState(new King(getColor(), getPosition()));
    }

    @Override
    public char getFenSymbol() {
        return getColor() == PieceColor.WHITE ? 'K' : 'k';
    }

    @Override
    public String getDisplaySymbol() {
        return getColor() == PieceColor.WHITE ? "\u2654" : "\u265A";
    }
}
