package model;

import java.io.Serializable;

public enum PieceColor implements Serializable {
    WHITE,
    BLACK;

    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    public int pawnDirection() {
        return this == WHITE ? -1 : 1;
    }
}
