package analysis;

public enum MoveClassification {
    BEST,
    GOOD,
    INACCURACY,
    MISTAKE,
    BLUNDER;

    public static MoveClassification fromEvaluationLoss(double lossInPawns) {
        if (lossInPawns < 0.2) {
            return BEST;
        }
        if (lossInPawns < 0.5) {
            return GOOD;
        }
        if (lossInPawns < 1.0) {
            return INACCURACY;
        }
        if (lossInPawns < 2.0) {
            return MISTAKE;
        }
        return BLUNDER;
    }
}
