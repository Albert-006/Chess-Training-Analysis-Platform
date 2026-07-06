package opening;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Opening implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<String> moves;
    private final String idea;

    public Opening(String name, List<String> moves, String idea) {
        this.name = name;
        this.moves = new ArrayList<>(moves);
        this.idea = idea;
    }

    public String getName() {
        return name;
    }

    public List<String> getMoves() {
        return Collections.unmodifiableList(moves);
    }

    public String getIdea() {
        return idea;
    }

    public String moveLine() {
        return String.join(" ", moves);
    }

    @Override
    public String toString() {
        return name + " - " + moveLine();
    }
}
