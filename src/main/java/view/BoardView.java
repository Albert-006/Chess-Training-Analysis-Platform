package view;

import engine.Board;
import model.Move;
import model.Piece;
import model.Position;
import storage.UserSettings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardView extends GridPane {
    private static final int TILE_SIZE = 72;

    private Board board;
    private Consumer<Move> moveHandler;
    private Position selected;
    private Position dragStart;
    private List<Position> highlighted = new ArrayList<>();
    private List<Position> hintSquares = new ArrayList<>();
    private Position lastMoveFrom;
    private Position lastMoveTo;
    private Position invalidSquare;
    private UserSettings settings;

    public BoardView(Board board, Consumer<Move> moveHandler) {
        this(board, moveHandler, new UserSettings());
    }

    public BoardView(Board board, Consumer<Move> moveHandler, UserSettings settings) {
        this.board = board;
        this.moveHandler = moveHandler;
        this.settings = settings != null ? settings : new UserSettings();
        setAlignment(Pos.CENTER);
        getStyleClass().add("board");
        render();
    }

    public void setBoard(Board board) {
        this.board = board;
        clearSelection();
        render();
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings != null ? settings : new UserSettings();
        render();
    }

    public void setMoveHandler(Consumer<Move> moveHandler) {
        this.moveHandler = moveHandler;
    }

    public void clearSelection() {
        selected = null;
        highlighted = new ArrayList<>();
        hintSquares = new ArrayList<>();
        invalidSquare = null;
    }

    public void setLastMove(Move move) {
        if (move != null) {
            this.lastMoveFrom = move.getFrom();
            this.lastMoveTo = move.getTo();
        } else {
            this.lastMoveFrom = null;
            this.lastMoveTo = null;
        }
    }

    public void clearLastMove() {
        this.lastMoveFrom = null;
        this.lastMoveTo = null;
    }

    public void setInvalidSquare(Position pos) {
        this.invalidSquare = pos;
        render();
    }

    public void highlightMove(Move move) {
        highlighted = new ArrayList<>();
        highlighted.add(move.getFrom());
        highlighted.add(move.getTo());
        selected = move.getFrom();
        render();
    }

    public void setHintSquares(List<Position> squares) {
        this.hintSquares = new ArrayList<>(squares);
        render();
    }

    public void render() {
        getChildren().clear();

        boolean showCoords = settings != null && settings.isShowCoordinates();
        int offsetRow = showCoords ? 1 : 0;
        int offsetCol = showCoords ? 1 : 0;

        if (showCoords) {
            // Rank labels on left (col 0)
            for (int r = 0; r < 8; r++) {
                Label rankLabel = new Label(String.valueOf(8 - r));
                rankLabel.getStyleClass().add("coord-label");
                rankLabel.setPrefSize(20, TILE_SIZE);
                rankLabel.setAlignment(Pos.CENTER);
                add(rankLabel, 0, r + 1);
            }
            // File labels at bottom (row 9)
            for (int c = 0; c < 8; c++) {
                Label fileLabel = new Label(String.valueOf((char) ('a' + c)));
                fileLabel.getStyleClass().add("coord-label");
                fileLabel.setPrefSize(TILE_SIZE, 20);
                fileLabel.setAlignment(Pos.CENTER);
                add(fileLabel, c + 1, 9);
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position position = new Position(row, col);
                StackPane tile = createTile(position);
                add(tile, col + offsetCol, row + offsetRow);
            }
        }
    }

    private StackPane createTile(Position position) {
        StackPane tile = new StackPane();
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        tile.setMinSize(TILE_SIZE, TILE_SIZE);
        tile.setMaxSize(TILE_SIZE, TILE_SIZE);
        tile.getStyleClass().add(squareClass(position));

        // Last move highlight
        if (settings != null && settings.isHighlightLastMove()) {
            if (position.equals(lastMoveFrom) || position.equals(lastMoveTo)) {
                tile.getStyleClass().add("last-move-square");
            }
        }

        // Selection highlight
        if (position.equals(selected)) {
            tile.getStyleClass().add("selected-square");
        }

        // Legal move highlight
        if (settings != null && settings.isHighlightLegalMoves() && highlighted.contains(position)) {
            tile.getStyleClass().add("legal-square");
        }

        // Hint squares
        if (hintSquares.contains(position)) {
            tile.getStyleClass().add("hint-square");
        }

        // Invalid move flash / indicator
        if (position.equals(invalidSquare)) {
            tile.getStyleClass().add("invalid-square");
        }

        // King in check highlight (only block colour changes to red, piece unchanged)
        Position checkedKing = getCheckedKingPosition();
        if (checkedKing != null && position.equals(checkedKing)) {
            tile.getStyleClass().add("check-square");
        }

        Piece piece = board.getPiece(position);
        if (piece != null) {
            if (settings != null && settings.getPieceStyle() == UserSettings.PieceStyle.UNICODE_SYMBOLS) {
                Label label = new Label(piece.getDisplaySymbol());
                label.setFont(Font.font("Segoe UI Symbol", 42));
                label.getStyleClass().add(piece.getColor().name().toLowerCase() + "-piece");
                tile.getChildren().add(label);
            } else {
                javafx.scene.Node pieceNode = PieceViewFactory.createPieceNode(piece, TILE_SIZE);
                tile.getChildren().add(pieceNode);
            }
        }

        tile.setOnMouseClicked(event -> handleClick(position));

        tile.setOnDragDetected(event -> {
            Piece draggingPiece = board.getPiece(position);
            if (draggingPiece == null || draggingPiece.getColor() != board.getSideToMove()) {
                return;
            }
            dragStart = position;
            selected = position;
            if (settings != null && settings.isHighlightLegalMoves()) {
                highlighted = targetsFrom(position);
            }
            render();

            Dragboard dragboard = tile.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(position.toAlgebraic());
            dragboard.setContent(content);
            event.consume();
        });

        tile.setOnDragOver(event -> {
            if (dragStart != null) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        tile.setOnDragDropped(event -> {
            boolean success = false;
            if (dragStart != null && moveHandler != null) {
                Position src = dragStart;
                Position dst = position;
                dragStart = null;
                clearSelection();
                moveHandler.accept(new Move(src, dst));
                success = true;
            } else {
                dragStart = null;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return tile;
    }

    private String squareClass(Position position) {
        boolean light = (position.getRow() + position.getCol()) % 2 == 0;
        String themePrefix = getBoardThemeClass();
        return light ? (themePrefix + "-light") : (themePrefix + "-dark");
    }

    private String getBoardThemeClass() {
        if (settings == null || settings.getBoardTheme() == null) {
            return "green";
        }
        switch (settings.getBoardTheme()) {
            case BLUE:
                return "blue";
            case WOOD:
                return "wood";
            case MONOCHROME:
                return "mono";
            case CLASSIC_GREEN:
            default:
                return "green";
        }
    }

    private void handleClick(Position position) {
        invalidSquare = null;
        Piece piece = board.getPiece(position);

        if (selected == null) {
            if (piece != null && piece.getColor() == board.getSideToMove()) {
                selected = position;
                hintSquares.clear();
                if (settings != null && settings.isHighlightLegalMoves()) {
                    highlighted = targetsFrom(position);
                }
                render();
            }
            return;
        }

        // If clicking the same selected piece, deselect
        if (position.equals(selected)) {
            clearSelection();
            render();
            return;
        }

        // If clicking another piece of the same color, switch selection
        if (piece != null && piece.getColor() == board.getSideToMove()) {
            selected = position;
            hintSquares.clear();
            if (settings != null && settings.isHighlightLegalMoves()) {
                highlighted = targetsFrom(position);
            }
            render();
            return;
        }

        // Attempting to move to position
        Position source = selected;
        clearSelection();
        render();

        if (moveHandler != null) {
            moveHandler.accept(new Move(source, position));
        }
    }

    private Position getCheckedKingPosition() {
        if (board == null) return null;
        for (model.PieceColor color : model.PieceColor.values()) {
            if (board.isInCheck(color)) {
                return board.findKing(color);
            }
        }
        return null;
    }

    private List<Position> targetsFrom(Position from) {
        List<Position> targets = new ArrayList<>();
        for (Move move : board.getLegalMovesFrom(from)) {
            targets.add(move.getTo());
        }
        return targets;
    }
}
