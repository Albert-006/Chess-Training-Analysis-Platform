package model;

import engine.Board;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    private static final long serialVersionUID = 1L;

    public Pawn(PieceColor color, Position position) {
        super(color, position, 100);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        int direction = getColor().pawnDirection();
        int startRow = getColor() == PieceColor.WHITE ? 6 : 1;
        int promotionRow = getColor() == PieceColor.WHITE ? 0 : 7;
        int row = getPosition().getRow();
        int col = getPosition().getCol();

        int oneForward = row + direction;
        if (Position.isValid(oneForward, col) && board.getPiece(new Position(oneForward, col)) == null) {
            addPawnMove(moves, new Position(oneForward, col), promotionRow);
            int twoForward = row + (direction * 2);
            if (row == startRow && board.getPiece(new Position(twoForward, col)) == null) {
                moves.add(new Move(getPosition(), new Position(twoForward, col)));
            }
        }

        for (int colDelta : new int[]{-1, 1}) {
            int captureCol = col + colDelta;
            if (!Position.isValid(oneForward, captureCol)) {
                continue;
            }
            Position target = new Position(oneForward, captureCol);
            Piece targetPiece = board.getPiece(target);
            if (targetPiece != null && targetPiece.getColor() != getColor() && !(targetPiece instanceof King)) {
                addPawnMove(moves, target, promotionRow);
            }
            if (target.equals(board.getEnPassantTarget())) {
                moves.add(Move.enPassant(getPosition(), target));
            }
        }
        return moves;
    }

    private void addPawnMove(List<Move> moves, Position target, int promotionRow) {
        if (target.getRow() == promotionRow) {
            moves.add(new Move(getPosition(), target, 'Q'));
            moves.add(new Move(getPosition(), target, 'R'));
            moves.add(new Move(getPosition(), target, 'B'));
            moves.add(new Move(getPosition(), target, 'N'));
        } else {
            moves.add(new Move(getPosition(), target));
        }
    }

    @Override
    public Piece copy() {
        return copyState(new Pawn(getColor(), getPosition()));
    }

    @Override
    public char getFenSymbol() {
        return getColor() == PieceColor.WHITE ? 'P' : 'p';
    }

    @Override
    public String getDisplaySymbol() {
        return getColor() == PieceColor.WHITE ? "\u2659" : "\u265F";
    }
}
