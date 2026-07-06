package puzzle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PuzzleValidatorTest {
    @Test
    void builtInMatePuzzleValidates() {
        Puzzle puzzle = new PuzzleService().getPuzzles()
                .stream()
                .filter(candidate -> candidate.getId().equals("mate-001"))
                .findFirst()
                .orElseThrow();

        assertTrue(new PuzzleValidator().validateAndPlayLegalMoves(puzzle, List.of("h6h7")));
    }
}
