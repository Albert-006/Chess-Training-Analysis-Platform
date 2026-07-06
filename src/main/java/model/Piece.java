package model;

import engine.Board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Piece implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PieceColor color;
    private Position position;
    private final int value;
    private boolean moved;

    protected Piece(PieceColor color, Position position, int value) {
        this.color = color;
        this.position = position;
        this.value = value;
    }

    public PieceColor getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getValue() {
        return value;
    }

    public boolean hasMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public abstract List<Move> getPseudoLegalMoves(Board board);

    public abstract Piece copy();

    public abstract char getFenSymbol();

    public abstract String getDisplaySymbol();

    protected <T extends Piece> T copyState(T copy) {
        copy.setMoved(moved);
        return copy;
    }

    protected List<Move> slidingMoves(Board board, int[][] directions) {
        List<Move> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int row = position.getRow() + direction[0];
            int col = position.getCol() + direction[1];
            while (Position.isValid(row, col)) {
                Position target = new Position(row, col);
                Piece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new Move(position, target));
                } else {
                    if (occupant.getColor() != color && !(occupant instanceof King)) {
                        moves.add(new Move(position, target));
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
        return moves;
    }

    protected void addStepMove(Board board, List<Move> moves, int rowDelta, int colDelta) {
        int row = position.getRow() + rowDelta;
        int col = position.getCol() + colDelta;
        if (!Position.isValid(row, col)) {
            return;
        }
        Position target = new Position(row, col);
        Piece occupant = board.getPiece(target);
        if (occupant == null || (occupant.getColor() != color && !(occupant instanceof King))) {
            moves.add(new Move(position, target));
        }
    }
}
