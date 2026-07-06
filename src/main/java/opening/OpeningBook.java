package opening;

import model.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpeningBook {
    private final List<Opening> openings = new ArrayList<>();

    public OpeningBook() {
        seedOpenings();
    }

    public List<Opening> getOpenings() {
        return new ArrayList<>(openings);
    }

    public Optional<Opening> findBestMatch(List<Move> moveHistory) {
        List<String> played = normalize(moveHistory);
        return openings.stream()
                .filter(opening -> isPrefix(opening.getMoves(), played))
                .max(Comparator.comparingInt(opening -> opening.getMoves().size()));
    }

    public Optional<Opening> findLikelyOpening(List<Move> moveHistory) {
        List<String> played = normalize(moveHistory);
        return openings.stream()
                .filter(opening -> isPrefix(played, opening.getMoves()))
                .max(Comparator.comparingInt(opening -> Math.min(opening.getMoves().size(), played.size())));
    }

    public String describeCurrentLine(List<Move> moveHistory) {
        Optional<Opening> exact = findBestMatch(moveHistory);
        if (exact.isPresent()) {
            return exact.get().getName();
        }
        return findLikelyOpening(moveHistory)
                .map(opening -> "Possible: " + opening.getName())
                .orElse("Unbooked position");
    }

    private List<String> normalize(List<Move> moveHistory) {
        return moveHistory.stream()
                .map(move -> move.coordinateNotation().toLowerCase())
                .collect(Collectors.toList());
    }

    private boolean isPrefix(List<String> prefix, List<String> whole) {
        if (prefix.size() > whole.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!prefix.get(i).equalsIgnoreCase(whole.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void seedOpenings() {
        openings.add(new Opening(
                "Sicilian Defense",
                moves("e2e4", "c7c5"),
                "Black fights for the center asymmetrically and aims for active counterplay."));
        openings.add(new Opening(
                "French Defense",
                moves("e2e4", "e7e6"),
                "Black supports d5 and accepts a compact structure with counterattacking chances."));
        openings.add(new Opening(
                "Ruy Lopez",
                moves("e2e4", "e7e5", "g1f3", "b8c6", "f1b5"),
                "White pressures the e5 pawn and builds long-term central pressure."));
        openings.add(new Opening(
                "Italian Game",
                moves("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"),
                "White develops quickly toward f7 and prepares a classical kingside attack."));
        openings.add(new Opening(
                "Queen's Gambit",
                moves("d2d4", "d7d5", "c2c4"),
                "White offers the c-pawn to deflect Black's central d-pawn."));
        openings.add(new Opening(
                "King's Indian Defense",
                moves("d2d4", "g8f6", "c2c4", "g7g6"),
                "Black allows White a broad center and prepares a kingside counterattack."));
    }

    private List<String> moves(String... moves) {
        return Arrays.stream(moves)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
