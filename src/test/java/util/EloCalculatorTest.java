package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EloCalculatorTest {
    @Test
    void equalRatedWinGainsHalfKFactor() {
        assertEquals(1216, EloCalculator.updateRating(1200, 1200, 1.0, 32));
    }

    @Test
    void equalRatedDrawKeepsRatingStable() {
        assertEquals(1200, EloCalculator.updateRating(1200, 1200, 0.5, 32));
    }
}
