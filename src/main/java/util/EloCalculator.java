package util;

public final class EloCalculator {
    private EloCalculator() {
    }

    public static int updateRating(int oldRating, int opponentRating, double score, int kFactor) {
        double expectedScore = expectedScore(oldRating, opponentRating);
        return (int) Math.round(oldRating + kFactor * (score - expectedScore));
    }

    public static double expectedScore(int rating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
    }
}
