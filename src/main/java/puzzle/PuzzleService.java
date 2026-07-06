package puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PuzzleService {
    private final List<Puzzle> puzzles = new ArrayList<>();
    private final Random random = new Random();

    public PuzzleService() {
        seedBuiltInPuzzles();
    }

    public List<Puzzle> getPuzzles() {
        return Collections.unmodifiableList(puzzles);
    }

    public Puzzle randomPuzzle() {
        return puzzles.get(random.nextInt(puzzles.size()));
    }

    private void seedBuiltInPuzzles() {
        puzzles.add(new Puzzle(
                "mate-001",
                "Mate in 1",
                "6k1/8/6KQ/8/8/8/8/8 w - - 0 1",
                moves("h6h7"),
                "Mate in 1",
                800));
        puzzles.add(new Puzzle(
                "mate-002",
                "Queen Net",
                "6k1/5ppp/8/8/8/8/5PPP/5RK1 w - - 0 1",
                moves("f1c1"),
                "Mate in 2",
                1050));
        puzzles.add(new Puzzle(
                "fork-001",
                "Knight Fork",
                "r3k2r/ppp2ppp/2npbn2/3Np3/2B1P3/8/PPPP1PPP/RNBQK2R w KQkq - 0 1",
                moves("d5f6"),
                "Fork",
                950));
        puzzles.add(new Puzzle(
                "pin-001",
                "Pin Pressure",
                "4r1k1/5ppp/8/8/8/8/4BPPP/4R1K1 w - - 0 1",
                moves("e2c4"),
                "Pin",
                900));
        puzzles.add(new Puzzle(
                "skewer-001",
                "Back Rank Skewer",
                "4k3/8/8/8/8/8/4R3/4K3 w - - 0 1",
                moves("e2e7"),
                "Skewer",
                700));
        puzzles.add(new Puzzle(
                "discovered-001",
                "Discovered Attack",
                "4k3/8/8/8/3B4/4N3/8/4K3 w - - 0 1",
                moves("e3f5"),
                "Discovered attack",
                850));
    }

    private List<String> moves(String... moves) {
        return Arrays.asList(moves);
    }
}
