package engine;

import model.Move;
import model.PieceColor;
import model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckmateDetectionTest {
    @Test
    void foolsMateIsDetectedAsCheckmate() {
        Board board = new Board();

        board.makeMove(move("f2", "f3"));
        board.makeMove(move("e7", "e5"));
        board.makeMove(move("g2", "g4"));
        board.makeMove(move("d8", "h4"));

        assertTrue(board.isCheckmate(PieceColor.WHITE));
    }

    private Move move(String from, String to) {
        return new Move(Position.fromAlgebraic(from), Position.fromAlgebraic(to));
    }
}
