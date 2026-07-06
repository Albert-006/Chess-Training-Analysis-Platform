package view;

import engine.Board;
import model.Move;
import model.Piece;
import model.Position;
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

    public BoardView(Board board, Consumer<Move> moveHandler) {
        this.board = board;
        this.moveHandler = moveHandler;
        setAlignment(Pos.CENTER);
        getStyleClass().add("board");
        render();
    }

    public void setBoard(Board board) {
        this.board = board;
        clearSelection();
        render();
    }

    public void setMoveHandler(Consumer<Move> moveHandler) {
        this.moveHandler = moveHandler;
    }

    public void clearSelection() {
        selected = null;
        highlighted = new ArrayList<>();
    }

    public void highlightMove(Move move) {
        highlighted = new ArrayList<>();
        highlighted.add(move.getFrom());
        highlighted.add(move.getTo());
        selected = move.getFrom();
        render();
    }

    public void render() {
        getChildren().clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position position = new Position(row, col);
                StackPane tile = createTile(position);
                add(tile, col, row);
            }
        }
    }

    private StackPane createTile(Position position) {
        StackPane tile = new StackPane();
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        tile.setMinSize(TILE_SIZE, TILE_SIZE);
        tile.setMaxSize(TILE_SIZE, TILE_SIZE);
        tile.getStyleClass().add(squareClass(position));
        if (position.equals(selected)) {
            tile.getStyleClass().add("selected-square");
        }
        if (highlighted.contains(position)) {
            tile.getStyleClass().add("legal-square");
        }

        Piece piece = board.getPiece(position);
        if (piece != null) {
            Label label = new Label(piece.getDisplaySymbol());
            label.setFont(Font.font("Segoe UI Symbol", 40));
            label.getStyleClass().add(piece.getColor().name().toLowerCase() + "-piece");
            tile.getChildren().add(label);
        }

        tile.setOnMouseClicked(event -> handleClick(position));
        tile.setOnDragDetected(event -> {
            Piece draggingPiece = board.getPiece(position);
            if (draggingPiece == null || draggingPiece.getColor() != board.getSideToMove()) {
                return;
            }
            dragStart = position;
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
                moveHandler.accept(new Move(dragStart, position));
                success = true;
            }
            dragStart = null;
            event.setDropCompleted(success);
            event.consume();
        });
        return tile;
    }

    private String squareClass(Position position) {
        boolean light = (position.getRow() + position.getCol()) % 2 == 0;
        return light ? "light-square" : "dark-square";
    }

    private void handleClick(Position position) {
        Piece piece = board.getPiece(position);
        if (selected == null) {
            if (piece != null && piece.getColor() == board.getSideToMove()) {
                selected = position;
                highlighted = targetsFrom(position);
                render();
            }
            return;
        }

        if (highlighted.contains(position) && moveHandler != null) {
            Move move = new Move(selected, position);
            clearSelection();
            moveHandler.accept(move);
            return;
        }

        if (piece != null && piece.getColor() == board.getSideToMove()) {
            selected = position;
            highlighted = targetsFrom(position);
        } else {
            clearSelection();
        }
        render();
    }

    private List<Position> targetsFrom(Position from) {
        List<Position> targets = new ArrayList<>();
        for (Move move : board.getLegalMovesFrom(from)) {
            targets.add(move.getTo());
        }
        return targets;
    }
}
