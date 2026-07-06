package puzzle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Puzzle implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String fen;
    private final List<String> solutionMoves;
    private final String motif;
    private final int rating;

    public Puzzle(String id, String title, String fen, List<String> solutionMoves, String motif, int rating) {
        this.id = id;
        this.title = title;
        this.fen = fen;
        this.solutionMoves = new ArrayList<>(solutionMoves);
        this.motif = motif;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFen() {
        return fen;
    }

    public List<String> getSolutionMoves() {
        return Collections.unmodifiableList(solutionMoves);
    }

    public String getMotif() {
        return motif;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return title + " (" + motif + ", " + rating + ")";
    }
}
