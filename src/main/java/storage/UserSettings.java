package storage;

import java.io.Serializable;

public class UserSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Theme {
        LIGHT, DARK, SYSTEM
    }

    public enum BoardTheme {
        CLASSIC_GREEN, BLUE, WOOD, MONOCHROME
    }

    public enum PieceStyle {
        STAUNTON_VECTOR, UNICODE_SYMBOLS
    }

    private Theme theme = Theme.DARK;
    private BoardTheme boardTheme = BoardTheme.CLASSIC_GREEN;
    private PieceStyle pieceStyle = PieceStyle.STAUNTON_VECTOR;
    private double uiScale = 1.0;

    private boolean highlightLegalMoves = true;
    private boolean highlightLastMove = true;
    private boolean showCoordinates = true;
    private boolean soundEnabled = false; // or animations

    public UserSettings() {
    }

    public Theme getTheme() {
        return theme != null ? theme : Theme.DARK;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public BoardTheme getBoardTheme() {
        return boardTheme != null ? boardTheme : BoardTheme.CLASSIC_GREEN;
    }

    public void setBoardTheme(BoardTheme boardTheme) {
        this.boardTheme = boardTheme;
    }

    public PieceStyle getPieceStyle() {
        return pieceStyle != null ? pieceStyle : PieceStyle.STAUNTON_VECTOR;
    }

    public void setPieceStyle(PieceStyle pieceStyle) {
        this.pieceStyle = pieceStyle;
    }

    public double getUiScale() {
        return uiScale > 0.5 && uiScale < 2.0 ? uiScale : 1.0;
    }

    public void setUiScale(double uiScale) {
        this.uiScale = uiScale;
    }

    public boolean isHighlightLegalMoves() {
        return highlightLegalMoves;
    }

    public void setHighlightLegalMoves(boolean highlightLegalMoves) {
        this.highlightLegalMoves = highlightLegalMoves;
    }

    public boolean isHighlightLastMove() {
        return highlightLastMove;
    }

    public void setHighlightLastMove(boolean highlightLastMove) {
        this.highlightLastMove = highlightLastMove;
    }

    public boolean isShowCoordinates() {
        return showCoordinates;
    }

    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
}
