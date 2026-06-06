public class SpawnIndicator extends GameObject {
    private long startTime;
    private final long duration = 1000; // 1 saniye (1000 ms) doğuş gecikmesi
    private Difficulty difficulty;
    private int stageLevel;

    public SpawnIndicator(int x, int y, Difficulty difficulty, int stageLevel) {
        // Tanklar 32x32 boyutunda olduğu için doğuş yıldızı da 32x32 boyutunda çizilecek
        super(x, y, 32, 32);
        this.difficulty = difficulty;
        this.stageLevel = stageLevel;
        this.startTime = System.currentTimeMillis();
    }

    // Göstergenin süresinin dolup dolmadığını (yani tankın doğma vaktinin gelip gelmediğini) kontrol eder
    public boolean isExpired() {
        return System.currentTimeMillis() - startTime >= duration;
    }

    // Geçen süreye göre hangi animasyon karesinin (0, 1, 2, 3) çizileceğini hesaplar
    public int getFrame() {
        long elapsed = System.currentTimeMillis() - startTime;
        // Yıldızın yanıp sönme hızını ayarlamak için her 125 ms'de bir kare değiştiriyoruz
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
