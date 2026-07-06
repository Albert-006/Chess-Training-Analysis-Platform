package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;
    private int eloRating = 1200;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private int puzzleRating = 1000;
    private String favoriteOpening = "Italian Game";

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getEloRating() {
        return eloRating;
    }

    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public int getPuzzleRating() {
        return puzzleRating;
    }

    public void setPuzzleRating(int puzzleRating) {
        this.puzzleRating = puzzleRating;
    }

    public String getFavoriteOpening() {
        return favoriteOpening;
    }

    public void setFavoriteOpening(String favoriteOpening) {
        this.favoriteOpening = favoriteOpening;
    }

    public void recordResult(double score) {
        gamesPlayed++;
        if (score == 1.0) {
            wins++;
        } else if (score == 0.5) {
            draws++;
        } else {
            losses++;
        }
    }
}
