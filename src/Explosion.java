// Mermi çarpışmalarında veya tank patlamalarında ekranda beliren patlama animasyonu sınıfı
public class Explosion extends GameObject {
    private boolean isBig;
    private int frameIndex = 0;
    private long lastFrameTime;
    private boolean expired = false;

    // Her kare 50 ms boyunca ekranda kalır
    private static final long FRAME_DELAY_MS = 50;

    public Explosion(int x, int y, boolean isBig) {
        // Büyük patlama 32x32, küçük patlama 16x16 boyutundadır
        super(x, y, 16, 16);
        this.isBig = isBig;
        if (isBig) {
            this.width = 32;
            this.height = 32;
            // Büyük patlama tankın tam ortalanması için x ve y koordinatlarını hafif kaydırırız
            this.x = x - 8;
            this.y = y - 8;
        }
        this.lastFrameTime = System.currentTimeMillis();
    }

    // Animasyon karesini günceller. Animasyon bittiğinde expired bayrağını true yapar.
    public void update() {
        if (expired) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= FRAME_DELAY_MS) {
            frameIndex++;
            lastFrameTime = currentTime;

            if (isBig) {
                // Büyük patlama 5 kareden oluşur (0..4)
                if (frameIndex >= 5) {
                    expired = true;
                }
            } else {
                // Küçük patlama 3 kareden oluşur (0..2)
                if (frameIndex >= 3) {
                    expired = true;
                }
            }
        }
    }

    // Çizilecek olan geçerli patlama kare indeksini döndürür
    public int getFrame() {
        return frameIndex;
    }

    // Patlama animasyonu bitti mi?
    public boolean isExpired() {
        return expired;
    }

    // Büyük patlama mı?
    public boolean isBig() {
        return isBig;
    }
}
