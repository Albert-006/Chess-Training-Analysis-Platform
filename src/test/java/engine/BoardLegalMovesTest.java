package engine;

import model.Move;
import model.PieceColor;
import model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardLegalMovesTest {
    @Test
    void startingPositionHasTwentyLegalMoves() {
        Board board = new Board();

        assertEquals(20, board.generateLegalMoves(PieceColor.WHITE).size());
    }

    @Test
    void pawnDoubleMoveCreatesEnPassantTarget() {
        Board board = new Board();

        assertTrue(board.makeMove(new Move(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"))));

        assertEquals(PieceColor.BLACK, board.getSideToMove());
        assertEquals(Position.fromAlgebraic("e3"), board.getEnPassantTarget());
    }

    @Test
    void illegalMoveIsRejected() {
        Board board = new Board();

        assertFalse(board.makeMove(new Move(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e5"))));
        assertEquals(PieceColor.WHITE, board.getSideToMove());
    }

    @Test
    void undoAndRedoRestoreMoveHistory() {
        Board board = new Board();
        Move move = new Move(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));

        board.makeMove(move);
        assertEquals(1, board.getMoveHistory().size());
        assertTrue(board.undo());
        assertEquals(0, board.getMoveHistory().size());
        assertTrue(board.redo());
        assertEquals(1, board.getMoveHistory().size());
    }
}
