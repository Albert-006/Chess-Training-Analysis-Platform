package analysis;

import model.Move;
import model.PieceColor;

public class MoveEvaluation {
    private final int moveNumber;
    private final PieceColor player;
    private final Move playedMove;
    private final Move bestMove;
    private final double playedEvaluation;
    private final double bestEvaluation;
    private final double evaluationLoss;
    private final MoveClassification classification;

    public MoveEvaluation(int moveNumber, PieceColor player, Move playedMove, Move bestMove,
                          double playedEvaluation, double bestEvaluation, double evaluationLoss,
                          MoveClassification classification) {
        this.moveNumber = moveNumber;
        this.player = player;
        this.playedMove = playedMove;
        this.bestMove = bestMove;
        this.playedEvaluation = playedEvaluation;
        this.bestEvaluation = bestEvaluation;
        this.evaluationLoss = evaluationLoss;
        this.classification = classification;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public PieceColor getPlayer() {
        return player;
    }

    public Move getPlayedMove() {
        return playedMove;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public double getPlayedEvaluation() {
        return playedEvaluation;
    }

    public double getBestEvaluation() {
        return bestEvaluation;
    }

    public double getEvaluationLoss() {
        return evaluationLoss;
    }

    public MoveClassification getClassification() {
        return classification;
    }
}
