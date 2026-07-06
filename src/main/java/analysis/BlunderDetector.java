package analysis;

import java.util.List;
import java.util.stream.Collectors;

public class BlunderDetector {
    public List<MoveEvaluation> criticalMoves(List<MoveEvaluation> evaluations) {
        return evaluations.stream()
                .filter(evaluation -> evaluation.getClassification() == MoveClassification.MISTAKE
                        || evaluation.getClassification() == MoveClassification.BLUNDER)
                .collect(Collectors.toList());
    }
}
