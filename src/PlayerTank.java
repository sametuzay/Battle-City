public class PlayerTank extends Tank {
    public int lives;

    // Kalkan (doğuş koruması) durumu
    private boolean shielded = false;
    private long shieldEndTime = 0;
    private int shieldFrame = 0;
    private long lastShieldAnimTime = 0;
    private static final long SHIELD_ANIM_DELAY_MS = 40; // Kalkan animasyon hızı (40 ms)

    // Yıldız (Star) powerup seviyesi (0, 1, 2 veya 3)
    private int starLevel = 0;
    public int maxBullets = 1; // Maksimum atılabilecek aktif mermi sayısı

    public PlayerTank(int x, int y, int width, int height, int speed, Direction direction) {
        super(x, y, width, height, 2, direction); // Oyuncu hızı taktiksel 2 yapıldı
        this.lives = 3;
        activateShield(3000); // Başlangıçta 3 saniye koruma
        this.lastShieldAnimTime = System.currentTimeMillis();
    }

    // Kalkanı belirli bir milisaniye süresince etkinleştirir
    public void activateShield(long durationMs) {
        this.shielded = true;
        this.shieldEndTime = System.currentTimeMillis() + durationMs;
    }

    // Kalkanın hala aktif olup olmadığını kontrol eder (süre dolunca otomatik deaktif eder)
    public boolean isShielded() {
        if (shielded && System.currentTimeMillis() > shieldEndTime) {
            shielded = false;
        }
        return shielded;
    }

    // Kalkan aktifken halkanın animasyonunu günceller (kare 0 ve 1 arasında geçiş yapar)
    public void updateShieldAnimation() {
        if (shielded) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShieldAnimTime >= SHIELD_ANIM_DELAY_MS) {
                if (shieldFrame == 0) {
                    shieldFrame = 1;
                } else {
                    shieldFrame = 0;
                }
                lastShieldAnimTime = currentTime;
            }
        }
    }

    // Geçerli kalkan animasyon karesini döndürür
    public int getShieldFrame() {
        return shieldFrame;
    }

    // Yıldız powerup'ı toplandığında seviyeyi artırır (max 3)
    public void collectStar() {
        if (starLevel < 3) {
            starLevel++;
        }
        // Seviye 2 veya üzerinde maksimum mermi sayısı 2 olur
        if (starLevel >= 2) {
            maxBullets = 2;
        } else {
            maxBullets = 1;
        }
    }

    // Yıldız seviyesini sıfırlar (oyuncu öldüğünde çağrılır)
    public void resetStarLevel() {
        starLevel = 0;
        maxBullets = 1;
    }

    // Mevcut yıldız seviyesini döner
    public int getStarLevel() {
        return starLevel;
    }

    @Override
    protected Bullet fireBullet() {
        int bulletX = this.x + (this.width / 2);
        int bulletY = this.y + (this.height / 2);

        int bulletSpeed = 6; // Varsayılan mermi hızı (Star level 0)
        if (starLevel >= 1) {
            bulletSpeed = 10; // Hızlı mermi (Star level 1, 2, 3)
        }

        boolean destroysSteel = false;
        if (starLevel >= 3) {
            destroysSteel = true; // Çelik kırıcı mermi (Star level 3)
        }

        return new Bullet(bulletX, bulletY, this.direction, false, bulletSpeed, destroysSteel);
    }
}

