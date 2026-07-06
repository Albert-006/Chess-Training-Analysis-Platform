package storage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SavedGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String username;
    private final List<String> moves;
    private final LocalDateTime date;
    private final String result;
    private final String opening;
    private final double accuracy;
    private final int playerRating;
    private final int opponentRating;

    public SavedGame(String id, String username, List<String> moves, LocalDateTime date, String result,
                     String opening, double accuracy, int playerRating, int opponentRating) {
        this.id = id;
        this.username = username;
        this.moves = new ArrayList<>(moves);
        this.date = date;
        this.result = result;
        this.opening = opening;
        this.accuracy = accuracy;
        this.playerRating = playerRating;
        this.opponentRating = opponentRating;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getMoves() {
        return Collections.unmodifiableList(moves);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getResult() {
        return result;
    }

    public String getOpening() {
        return opening;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int getPlayerRating() {
        return playerRating;
    }

    public int getOpponentRating() {
        return opponentRating;
    }

    @Override
    public String toString() {
        return date.toLocalDate() + " " + result + " - " + opening + " (" + moves.size() + " plies)";
    }
}
