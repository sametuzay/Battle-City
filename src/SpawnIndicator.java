public class SpawnIndicator extends GameObject {
    private long startTime;
    private final long duration = 1000;
    private Difficulty difficulty;
    private int stageLevel;

    public SpawnIndicator(int x, int y, Difficulty difficulty, int stageLevel) {
        super(x, y, 32, 32);
        this.difficulty = difficulty;
        this.stageLevel = stageLevel;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime >= duration;
    }

    public int getFrame() {
        long elapsed = System.currentTimeMillis() - startTime;
        int frameIndex = (int) ((elapsed / 125) % 4);
        return frameIndex;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getStageLevel() {
        return stageLevel;
    }
}
