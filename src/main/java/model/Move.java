package model;

import java.io.Serializable;
import java.util.Objects;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Position from;
    private final Position to;
    private final char promotion;
    private final boolean castling;
    private final boolean enPassant;
    private final String notation;

    public Move(Position from, Position to) {
        this(from, to, '\0', false, false, null);
    }

    public Move(Position from, Position to, char promotion) {
        this(from, to, promotion, false, false, null);
    }

    public Move(Position from, Position to, char promotion, boolean castling, boolean enPassant, String notation) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.promotion = normalizePromotion(promotion);
        this.castling = castling;
        this.enPassant = enPassant;
        this.notation = notation;
    }

    private static char normalizePromotion(char piece) {
        if (piece == '\0' || Character.isWhitespace(piece)) {
            return '\0';
        }
        return Character.toUpperCase(piece);
    }

    public static Move castle(Position from, Position to) {
        return new Move(from, to, '\0', true, false, null);
    }

    public static Move enPassant(Position from, Position to) {
        return new Move(from, to, '\0', false, true, null);
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public char getPromotion() {
        return promotion;
    }

    public boolean isCastling() {
        return castling;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public String getNotation() {
        return notation;
    }

    public Move withNotation(String newNotation) {
        return new Move(from, to, promotion, castling, enPassant, newNotation);
    }

    public Move asCastling() {
        return new Move(from, to, promotion, true, enPassant, notation);
    }

    public Move asEnPassant() {
        return new Move(from, to, promotion, castling, true, notation);
    }

    public boolean sameIntent(Move other) {
        if (other == null) {
            return false;
        }
        boolean promotionMatches = promotion == other.promotion
                || promotion == '\0'
                || other.promotion == '\0';
        return from.equals(other.from) && to.equals(other.to) && promotionMatches;
    }

    public String coordinateNotation() {
        String suffix = promotion == '\0' ? "" : String.valueOf(Character.toLowerCase(promotion));
        return from.toAlgebraic() + to.toAlgebraic() + suffix;
    }

    @Override
    public String toString() {
        return notation == null ? coordinateNotation() : notation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Move)) {
            return false;
        }
        Move move = (Move) o;
        return promotion == move.promotion
                && castling == move.castling
                && enPassant == move.enPassant
                && from.equals(move.from)
                && to.equals(move.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotion, castling, enPassant);
    }
}
