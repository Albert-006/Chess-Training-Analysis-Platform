package ai;

import engine.Board;
import model.Move;
import model.PieceColor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AlphaBetaAITest {
    @Test
    void aiFindsMateInOne() {
        Board board = Board.fromFen("6k1/8/6KQ/8/8/8/8/8 w - - 0 1");
        AlphaBetaAI ai = new AlphaBetaAI(1);

        Optional<Move> move = ai.chooseMove(board, PieceColor.WHITE);

        assertTrue(move.isPresent());
        assertTrue(board.makeMove(move.get()));
        assertTrue(board.isCheckmate(PieceColor.BLACK));
    }
}
