package ai;

public enum AIDifficulty {
    EASY(1),
    MEDIUM(3),
    HARD(5);

    private final int depth;

    AIDifficulty(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
