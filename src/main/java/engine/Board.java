package engine;

import model.Bishop;
import model.King;
import model.Knight;
import model.Move;
import model.Pawn;
import model.Piece;
import model.PieceColor;
import model.Position;
import model.Queen;
import model.Rook;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private Piece[][] squares = new Piece[8][8];
    private PieceColor sideToMove = PieceColor.WHITE;
    private Position enPassantTarget;
    private List<Move> moveHistory = new ArrayList<>();
    private Deque<BoardSnapshot> undoStack = new ArrayDeque<>();
    private Deque<BoardSnapshot> redoStack = new ArrayDeque<>();

    public Board() {
        setupStandardBoard();
    }

    private Board(boolean setup) {
        if (setup) {
            setupStandardBoard();
        }
    }

    public final void setupStandardBoard() {
        clear();
        placeBackRank(PieceColor.BLACK, 0);
        placePawns(PieceColor.BLACK, 1);
        placePawns(PieceColor.WHITE, 6);
        placeBackRank(PieceColor.WHITE, 7);
        sideToMove = PieceColor.WHITE;
        enPassantTarget = null;
        moveHistory.clear();
        undoStack.clear();
        redoStack.clear();
    }

    public void clear() {
        squares = new Piece[8][8];
        sideToMove = PieceColor.WHITE;
        enPassantTarget = null;
        moveHistory.clear();
        undoStack.clear();
        redoStack.clear();
    }

    private void placeBackRank(PieceColor color, int row) {
        setPiece(new Position(row, 0), new Rook(color, new Position(row, 0)));
        setPiece(new Position(row, 1), new Knight(color, new Position(row, 1)));
        setPiece(new Position(row, 2), new Bishop(color, new Position(row, 2)));
        setPiece(new Position(row, 3), new Queen(color, new Position(row, 3)));
        setPiece(new Position(row, 4), new King(color, new Position(row, 4)));
        setPiece(new Position(row, 5), new Bishop(color, new Position(row, 5)));
        setPiece(new Position(row, 6), new Knight(color, new Position(row, 6)));
        setPiece(new Position(row, 7), new Rook(color, new Position(row, 7)));
    }

    private void placePawns(PieceColor color, int row) {
        for (int col = 0; col < 8; col++) {
            setPiece(new Position(row, col), new Pawn(color, new Position(row, col)));
        }
    }

    public Piece getPiece(Position position) {
        return squares[position.getRow()][position.getCol()];
    }

    public void setPiece(Position position, Piece piece) {
        squares[position.getRow()][position.getCol()] = piece;
        if (piece != null) {
            piece.setPosition(position);
        }
    }

    public PieceColor getSideToMove() {
        return sideToMove;
    }

    public void setSideToMove(PieceColor sideToMove) {
        this.sideToMove = sideToMove;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public List<Piece> getPieces(PieceColor color) {
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getColor() == color) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }

    public List<Move> generateLegalMoves(PieceColor color) {
        List<Move> moves = new ArrayList<>();
        for (Piece piece : getPieces(color)) {
            for (Move move : piece.getPseudoLegalMoves(this)) {
                if (!wouldLeaveKingInCheck(color, move)) {
                    moves.add(move.withNotation(move.coordinateNotation()));
                }
            }
        }
        return moves;
    }

    public List<Move> getLegalMovesFrom(Position from) {
        Piece piece = getPiece(from);
        if (piece == null || piece.getColor() != sideToMove) {
            return Collections.emptyList();
        }
        List<Move> moves = new ArrayList<>();
        for (Move move : generateLegalMoves(sideToMove)) {
            if (move.getFrom().equals(from)) {
                moves.add(move);
            }
        }
        return moves;
    }

    public boolean isLegalMove(Move move) {
        return findLegalMove(move).isPresent();
    }

    public Optional<Move> findLegalMove(Move move) {
        return generateLegalMoves(sideToMove)
                .stream()
                .filter(candidate -> candidate.sameIntent(move))
                .findFirst();
    }

    public boolean makeMove(Move move) {
        Optional<Move> legalMove = findLegalMove(move);
        if (legalMove.isEmpty()) {
            return false;
        }
        undoStack.push(snapshot());
        redoStack.clear();
        applyMoveUnchecked(legalMove.get(), true);
        return true;
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        redoStack.push(snapshot());
        restore(undoStack.pop());
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        undoStack.push(snapshot());
        restore(redoStack.pop());
        return true;
    }

    public boolean isInCheck(PieceColor color) {
        Position king = findKing(color);
        return king != null && isSquareAttacked(king, color.opposite());
    }

    public boolean isCheckmate(PieceColor color) {
        return isInCheck(color) && generateLegalMoves(color).isEmpty();
    }

    public boolean isStalemate(PieceColor color) {
        return !isInCheck(color) && generateLegalMoves(color).isEmpty();
    }

    public GameStatus getGameStatus() {
        if (isCheckmate(PieceColor.WHITE)) {
            return GameStatus.BLACK_WON;
        }
        if (isCheckmate(PieceColor.BLACK)) {
            return GameStatus.WHITE_WON;
        }
        if (isStalemate(sideToMove)) {
            return GameStatus.DRAW_STALEMATE;
        }
        return GameStatus.ACTIVE;
    }

    public Position findKing(PieceColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece instanceof King && piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    public boolean canCastle(PieceColor color, boolean kingSide) {
        int row = color == PieceColor.WHITE ? 7 : 0;
        Position kingPosition = new Position(row, 4);
        Piece king = getPiece(kingPosition);
        if (!(king instanceof King) || king.hasMoved() || isInCheck(color)) {
            return false;
        }

        int rookCol = kingSide ? 7 : 0;
        Piece rook = getPiece(new Position(row, rookCol));
        if (!(rook instanceof Rook) || rook.getColor() != color || rook.hasMoved()) {
            return false;
        }

        int[] emptyCols = kingSide ? new int[]{5, 6} : new int[]{1, 2, 3};
        for (int col : emptyCols) {
            if (getPiece(new Position(row, col)) != null) {
                return false;
            }
        }

        int[] kingPath = kingSide ? new int[]{5, 6} : new int[]{3, 2};
        for (int col : kingPath) {
            if (isSquareAttacked(new Position(row, col), color.opposite())) {
                return false;
            }
        }
        return true;
    }

    public boolean isSquareAttacked(Position square, PieceColor byColor) {
        for (Piece piece : getPieces(byColor)) {
            Position from = piece.getPosition();
            if (piece instanceof Pawn) {
                int attackedRow = from.getRow() + byColor.pawnDirection();
                if (attackedRow == square.getRow()
                        && Math.abs(from.getCol() - square.getCol()) == 1) {
                    return true;
                }
            } else if (piece instanceof Knight) {
                int rowDiff = Math.abs(from.getRow() - square.getRow());
                int colDiff = Math.abs(from.getCol() - square.getCol());
                if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
                    return true;
                }
            } else if (piece instanceof King) {
                int rowDiff = Math.abs(from.getRow() - square.getRow());
                int colDiff = Math.abs(from.getCol() - square.getCol());
                if (Math.max(rowDiff, colDiff) == 1) {
                    return true;
                }
            } else {
                if (piece instanceof Bishop || piece instanceof Queen) {
                    if (attacksOnRay(from, square, new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}})) {
                        return true;
                    }
                }
                if (piece instanceof Rook || piece instanceof Queen) {
                    if (attacksOnRay(from, square, new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}})) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean attacksOnRay(Position from, Position square, int[][] directions) {
        for (int[] direction : directions) {
            int row = from.getRow() + direction[0];
            int col = from.getCol() + direction[1];
            while (Position.isValid(row, col)) {
                Position current = new Position(row, col);
                if (current.equals(square)) {
                    return true;
                }
                if (getPiece(current) != null) {
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
        return false;
    }

    private boolean wouldLeaveKingInCheck(PieceColor color, Move move) {
        Board testBoard = copy();
        testBoard.applyMoveUnchecked(move, false);
        return testBoard.isInCheck(color);
    }

    private void applyMoveUnchecked(Move move, boolean recordHistory) {
        Piece movingPiece = getPiece(move.getFrom());
        if (movingPiece == null) {
            return;
        }

        setPiece(move.getFrom(), null);

        if (move.isEnPassant()) {
            Position capturedPawn = new Position(move.getFrom().getRow(), move.getTo().getCol());
            setPiece(capturedPawn, null);
        }

        if (move.isCastling()) {
            moveCastlingRook(move);
        }

        Piece pieceToPlace = movingPiece;
        if (movingPiece instanceof Pawn && isPromotionRank(move.getTo(), movingPiece.getColor())) {
            pieceToPlace = createPromotionPiece(movingPiece.getColor(), move.getTo(), move.getPromotion());
        }

        pieceToPlace.setMoved(true);
        setPiece(move.getTo(), pieceToPlace);

        enPassantTarget = null;
        if (movingPiece instanceof Pawn && Math.abs(move.getFrom().getRow() - move.getTo().getRow()) == 2) {
            int middleRow = (move.getFrom().getRow() + move.getTo().getRow()) / 2;
            enPassantTarget = new Position(middleRow, move.getFrom().getCol());
        }

        Move recordedMove = move.withNotation(move.coordinateNotation());
        if (recordHistory) {
            moveHistory.add(recordedMove);
        }
        sideToMove = sideToMove.opposite();
    }

    private void moveCastlingRook(Move move) {
        int row = move.getFrom().getRow();
        boolean kingSide = move.getTo().getCol() == 6;
        Position rookFrom = new Position(row, kingSide ? 7 : 0);
        Position rookTo = new Position(row, kingSide ? 5 : 3);
        Piece rook = getPiece(rookFrom);
        setPiece(rookFrom, null);
        if (rook != null) {
            rook.setMoved(true);
            setPiece(rookTo, rook);
        }
    }

    private boolean isPromotionRank(Position position, PieceColor color) {
        return (color == PieceColor.WHITE && position.getRow() == 0)
                || (color == PieceColor.BLACK && position.getRow() == 7);
    }

    private Piece createPromotionPiece(PieceColor color, Position position, char promotion) {
        char piece = promotion == '\0' ? 'Q' : Character.toUpperCase(promotion);
        switch (piece) {
            case 'R':
                return new Rook(color, position);
            case 'B':
                return new Bishop(color, position);
            case 'N':
                return new Knight(color, position);
            case 'Q':
            default:
                return new Queen(color, position);
        }
    }

    public Board copy() {
        Board copy = new Board(false);
        copy.squares = copySquares(squares);
        copy.sideToMove = sideToMove;
        copy.enPassantTarget = enPassantTarget;
        copy.moveHistory = new ArrayList<>(moveHistory);
        return copy;
    }

    private BoardSnapshot snapshot() {
        return new BoardSnapshot(copySquares(squares), sideToMove, enPassantTarget, new ArrayList<>(moveHistory));
    }

    private void restore(BoardSnapshot snapshot) {
        squares = copySquares(snapshot.squares);
        sideToMove = snapshot.sideToMove;
        enPassantTarget = snapshot.enPassantTarget;
        moveHistory = new ArrayList<>(snapshot.moveHistory);
    }

    private Piece[][] copySquares(Piece[][] source) {
        Piece[][] copied = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = source[row][col];
                copied[row][col] = piece == null ? null : piece.copy();
            }
        }
        return copied;
    }

    public static Board fromFen(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new IllegalArgumentException("FEN cannot be blank");
        }
        String[] fields = fen.trim().split("\\s+");
        Board board = new Board(false);
        board.clear();
        String[] ranks = fields[0].split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("FEN must have 8 ranks");
        }
        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char symbol : ranks[row].toCharArray()) {
                if (Character.isDigit(symbol)) {
                    col += Character.getNumericValue(symbol);
                } else {
                    Position position = new Position(row, col);
                    Piece piece = createPieceFromFen(symbol, position);
                    if (piece instanceof King || piece instanceof Rook) {
                        piece.setMoved(true);
                    }
                    board.setPiece(position, piece);
                    col++;
                }
            }
        }
        board.sideToMove = fields.length > 1 && "b".equals(fields[1]) ? PieceColor.BLACK : PieceColor.WHITE;
        if (fields.length > 2) {
            board.applyCastleRights(fields[2]);
        }
        if (fields.length > 3 && !"-".equals(fields[3])) {
            board.enPassantTarget = Position.fromAlgebraic(fields[3]);
        }
        return board;
    }

    private static Piece createPieceFromFen(char symbol, Position position) {
        PieceColor color = Character.isUpperCase(symbol) ? PieceColor.WHITE : PieceColor.BLACK;
        switch (Character.toLowerCase(symbol)) {
            case 'k':
                return new King(color, position);
            case 'q':
                return new Queen(color, position);
            case 'r':
                return new Rook(color, position);
            case 'b':
                return new Bishop(color, position);
            case 'n':
                return new Knight(color, position);
            case 'p':
                return new Pawn(color, position);
            default:
                throw new IllegalArgumentException("Unknown FEN symbol: " + symbol);
        }
    }

    private void applyCastleRights(String rights) {
        if (rights.contains("K")) {
            markUnmoved(new Position(7, 4));
            markUnmoved(new Position(7, 7));
        }
        if (rights.contains("Q")) {
            markUnmoved(new Position(7, 4));
            markUnmoved(new Position(7, 0));
        }
        if (rights.contains("k")) {
            markUnmoved(new Position(0, 4));
            markUnmoved(new Position(0, 7));
        }
        if (rights.contains("q")) {
            markUnmoved(new Position(0, 4));
            markUnmoved(new Position(0, 0));
        }
    }

    private void markUnmoved(Position position) {
        Piece piece = getPiece(position);
        if (piece != null) {
            piece.setMoved(false);
        }
    }

    public String toFen() {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(piece.getFenSymbol());
                }
            }
            if (empty > 0) {
                fen.append(empty);
            }
            if (row < 7) {
                fen.append('/');
            }
        }
        fen.append(sideToMove == PieceColor.WHITE ? " w " : " b ");
        String rights = castleRights();
        fen.append(rights.isEmpty() ? "-" : rights);
        fen.append(' ');
        fen.append(enPassantTarget == null ? "-" : enPassantTarget.toAlgebraic());
        fen.append(" 0 1");
        return fen.toString();
    }

    private String castleRights() {
        StringBuilder rights = new StringBuilder();
        appendCastleRight(rights, PieceColor.WHITE, true, "K");
        appendCastleRight(rights, PieceColor.WHITE, false, "Q");
        appendCastleRight(rights, PieceColor.BLACK, true, "k");
        appendCastleRight(rights, PieceColor.BLACK, false, "q");
        return rights.toString();
    }

    private void appendCastleRight(StringBuilder rights, PieceColor color, boolean kingSide, String symbol) {
        int row = color == PieceColor.WHITE ? 7 : 0;
        Piece king = getPiece(new Position(row, 4));
        Piece rook = getPiece(new Position(row, kingSide ? 7 : 0));
        if (king instanceof King && rook instanceof Rook
                && king.getColor() == color && rook.getColor() == color
                && !king.hasMoved() && !rook.hasMoved()) {
            rights.append(symbol);
        }
    }

    private static class BoardSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Piece[][] squares;
        private final PieceColor sideToMove;
        private final Position enPassantTarget;
        private final List<Move> moveHistory;

        private BoardSnapshot(Piece[][] squares, PieceColor sideToMove, Position enPassantTarget, List<Move> moveHistory) {
            this.squares = squares;
            this.sideToMove = sideToMove;
            this.enPassantTarget = enPassantTarget;
            this.moveHistory = moveHistory;
        }
    }
}
