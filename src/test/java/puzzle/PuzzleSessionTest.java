package puzzle;

import model.Move;
import model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PuzzleSessionTest {
    private PuzzleService puzzleService;

    @BeforeEach
    void setUp() {
        puzzleService = new PuzzleService();
    }

    @Test
    void testMateInOnePuzzleSolving() {
        Puzzle matePuzzle = puzzleService.getPuzzles().stream()
                .filter(p -> p.getId().equals("mate-001"))
                .findFirst()
                .orElseThrow();

        PuzzleSession session = new PuzzleSession(matePuzzle);
        assertFalse(session.isCompleted());
        assertEquals(0, session.getCurrentStep());

        // Valid move is h6h7
        Move solutionMove = new Move(Position.fromAlgebraic("h6"), Position.fromAlgebraic("h7"));
        PuzzleSession.MoveResult result = session.playMove(solutionMove);

        assertEquals(PuzzleSession.MoveResult.CORRECT_AND_COMPLETED, result);
        assertTrue(session.isCompleted());
        assertFalse(session.isFailed());
    }

    @Test
    void testInvalidPuzzleMoveIsRejected() {
        Puzzle matePuzzle = puzzleService.getPuzzles().stream()
                .filter(p -> p.getId().equals("mate-001"))
                .findFirst()
                .orElseThrow();

        PuzzleSession session = new PuzzleSession(matePuzzle);

        // Move queen illegally or playing illegal destination
        Move illegalMove = new Move(Position.fromAlgebraic("h6"), Position.fromAlgebraic("a1"));
        PuzzleSession.MoveResult result = session.playMove(illegalMove);
        assertEquals(PuzzleSession.MoveResult.ILLEGAL, result);
        assertFalse(session.isCompleted());

        // Legal chess move on board, but wrong puzzle solution move
        // Queen h6 to g7 is legal check, but wrong answer if solution is h6h7
        Move wrongLegalMove = new Move(Position.fromAlgebraic("h6"), Position.fromAlgebraic("g7"));
        if (session.getBoard().isLegalMove(wrongLegalMove)) {
            PuzzleSession.MoveResult incorrectResult = session.playMove(wrongLegalMove);
            assertEquals(PuzzleSession.MoveResult.INCORRECT, incorrectResult);
            assertTrue(session.isFailed());
            assertFalse(session.isCompleted());
        }
    }

    @Test
    void testHintGenerationReturnsNextMoveWithoutAlteringBoard() {
        Puzzle puzzle = puzzleService.getPuzzles().get(0);
        PuzzleSession session = new PuzzleSession(puzzle);

        String initialFen = session.getBoard().toFen();

        Optional<Move> hintOpt = session.getNextExpectedMove();
        assertTrue(hintOpt.isPresent());
        assertEquals("h6h7", hintOpt.get().coordinateNotation());

        // Board must not be altered by requesting hint
        assertEquals(initialFen, session.getBoard().toFen());
        assertFalse(session.isCompleted());
        assertEquals(0, session.getCurrentStep());
    }

    @Test
    void testPuzzleResetRestoresInitialPosition() {
        Puzzle matePuzzle = puzzleService.getPuzzles().stream()
                .filter(p -> p.getId().equals("mate-001"))
                .findFirst()
                .orElseThrow();

        PuzzleSession session = new PuzzleSession(matePuzzle);
        String initialFen = session.getBoard().toFen();

        Move solutionMove = new Move(Position.fromAlgebraic("h6"), Position.fromAlgebraic("h7"));
        session.playMove(solutionMove);
        assertTrue(session.isCompleted());

        session.reset();
        assertFalse(session.isCompleted());
        assertFalse(session.isFailed());
        assertEquals(0, session.getCurrentStep());
        assertEquals(initialFen, session.getBoard().toFen());
    }
}
