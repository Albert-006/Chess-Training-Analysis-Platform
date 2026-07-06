package analysis;

import ai.AlphaBetaAI;
import ai.EvaluationFunction;
import engine.Board;
import model.Move;
import model.PieceColor;

import java.util.Optional;

public class MoveEvaluator {
    private final AlphaBetaAI ai;
    private final EvaluationFunction evaluator;

    public MoveEvaluator(int searchDepth) {
        this.evaluator = new EvaluationFunction();
        this.ai = new AlphaBetaAI(searchDepth);
    }

    public MoveEvaluation evaluate(Board board, Move playedMove, int moveNumber) {
        PieceColor player = board.getSideToMove();
        Optional<Move> bestMove = ai.chooseMove(board, player);

        double bestEval = 0;
        if (bestMove.isPresent()) {
            Board bestBoard = board.copy();
            bestBoard.makeMove(bestMove.get());
            bestEval = evaluator.pawns(evaluator.evaluate(bestBoard, player));
        }

        Board playedBoard = board.copy();
        Move legalPlayedMove = board.findLegalMove(playedMove).orElse(playedMove);
        playedBoard.makeMove(legalPlayedMove);
        double playedEval = evaluator.pawns(evaluator.evaluate(playedBoard, player));
        double loss = Math.max(0.0, bestEval - playedEval);
        MoveClassification classification = MoveClassification.fromEvaluationLoss(loss);

        return new MoveEvaluation(
                moveNumber,
                player,
                legalPlayedMove,
                bestMove.orElse(null),
                playedEval,
                bestEval,
                loss,
                classification);
    }
}
