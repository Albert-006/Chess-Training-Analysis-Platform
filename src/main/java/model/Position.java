package model;

import java.io.Serializable;
import java.util.Objects;

public final class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;

    public Position(int row, int col) {
        if (!isValid(row, col)) {
            throw new IllegalArgumentException("Position must be inside the chess board: " + row + "," + col);
        }
        this.row = row;
        this.col = col;
    }

    public static boolean isValid(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public static Position fromAlgebraic(String square) {
        if (square == null || square.length() != 2) {
            throw new IllegalArgumentException("Square must use algebraic notation, for example e4");
        }
        char file = Character.toLowerCase(square.charAt(0));
        char rank = square.charAt(1);
        int col = file - 'a';
        int row = 8 - Character.getNumericValue(rank);
        return new Position(row, col);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Position offset(int rowDelta, int colDelta) {
        return new Position(row + rowDelta, col + colDelta);
    }

    public String toAlgebraic() {
        return String.valueOf((char) ('a' + col)) + (8 - row);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
