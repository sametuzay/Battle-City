public class Explosion extends GameObject {
    private boolean isBig;
    private int frameIndex = 0;
    private long lastFrameTime;
    private boolean expired = false;

    private static final long FRAME_DELAY_MS = 50;

    public Explosion(int x, int y, boolean isBig) {
        super(x, y, 16, 16);
        this.isBig = isBig;
        if (isBig) {
            this.width = 32;
            this.height = 32;
            this.x = x - 8;
            this.y = y - 8;
        }
        this.lastFrameTime = System.currentTimeMillis();
    }

    public void update() {
        if (expired) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= FRAME_DELAY_MS) {
            frameIndex++;
            lastFrameTime = currentTime;

            if (isBig) {
                if (frameIndex >= 5) {
                    expired = true;
                }
            } else {
                if (frameIndex >= 3) {
                    expired = true;
                }
            }
        }
    }

    public int getFrame() {
        return frameIndex;
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isBig() {
        return isBig;
    }
}
