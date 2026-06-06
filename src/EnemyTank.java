import java.util.Random;

public class EnemyTank extends Tank {
    private Random random;
    private long lastShootTime;
    private int shootInterval; // Ateş kontrol aralığı (milisaniye cinsinden)
    private int shootChance; // Ateş etme olasılığı (yüzde bazında, 1-100 arası)
    private int bulletSpeed = 6; // Standart düşman mermi hızı

    public EnemyTank(int x, int y, Difficulty difficulty, int stageLevel) {
        super(x, y, 32, 32, 1, Direction.DOWN);

        // Seçilen zorluk seviyesine göre temel değerler ayarlanır
        int baseSpeed = 1;
        int baseShootInterval = 600;
        int baseShootChance = 50;

        if (difficulty == Difficulty.EASY) {
            baseSpeed = 1;
            baseShootInterval = 800;
            baseShootChance = 30; // %30 ateş şansı
        } else if (difficulty == Difficulty.MEDIUM) {
            baseSpeed = 1;
            baseShootInterval = 600;
            baseShootChance = 50; // %50 ateş şansı
        } else if (difficulty == Difficulty.HARD) {
            baseSpeed = 2;
            baseShootInterval = 450;
            baseShootChance = 65; // %65 ateş şansı
        }

        // Harita (stage) ilerledikçe zorluk artışı (her stage için abartısız bonuslar)
        int stageBonus = stageLevel - 1; // Stage 1 = 0, Stage 2 = 1, Stage 3 = 2

        // Stage ilerledikçe hız artırılır (maksimum hız 2 ile sınırlandırılır)
        int speedBonus = 0;
        if (stageBonus > 0) {
            speedBonus = 1;
        }
        this.speed = Math.min(2, baseSpeed + speedBonus);

        // Stage ilerledikçe ateş etme sıklığı ve olasılığı artırılır
        this.shootInterval = Math.max(250, baseShootInterval - (stageBonus * 75)); // Her stage için aralık 75 ms kısalır
        this.shootChance = Math.min(85, baseShootChance + (stageBonus * 10));     // Her stage için olasılık %10 artar

        this.random = new Random();
        this.lastShootTime = System.currentTimeMillis();
    }

    @Override
    protected Bullet fireBullet() {
        int bulletX = this.x + (this.width / 2);
        int bulletY = this.y + (this.height / 2);
        // Standart hızda düşman mermisi fırlatır (çelik kıramaz=false)
        return new Bullet(bulletX, bulletY, this.direction, true, this.bulletSpeed, false);
    }

    public void randomMovement(int baseX, int baseY, int playerX, int playerY) {
        int choice = random.nextInt(100);

        if (choice < 70) {
            // %70 ihtimalle üsse doğru yönel
            int diffX = baseX - this.x;
            int diffY = baseY - this.y;

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (diffX > 0) {
                    this.direction = Direction.RIGHT;
                } else {
                    this.direction = Direction.LEFT;
                }
            } else {
                if (diffY > 0) {
                    this.direction = Direction.DOWN;
                } else {
                    this.direction = Direction.UP;
                }
            }
        } else if (choice < 85) {
            // %15 ihtimalle oyuncuya doğru yönel
            int diffX = playerX - this.x;
            int diffY = playerY - this.y;

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (diffX > 0) {
                    this.direction = Direction.RIGHT;
                } else {
                    this.direction = Direction.LEFT;
                }
            } else {
                if (diffY > 0) {
                    this.direction = Direction.DOWN;
                } else {
                    this.direction = Direction.UP;
                }
            }
        } else {
            // %15 ihtimalle tamamen rastgele yön seç
            int dir = random.nextInt(4);
            if (dir == 0) {
                this.direction = Direction.UP;
            } else if (dir == 1) {
                this.direction = Direction.DOWN;
            } else if (dir == 2) {
                this.direction = Direction.LEFT;
            } else if (dir == 3) {
                this.direction = Direction.RIGHT;
            }
        }
    }

    // Eski metot imzalarını uyumluluk adına koruyoruz
    public void randomMovement(int baseX, int baseY) {
        randomMovement(baseX, baseY, this.x, this.y);
    }

    public void randomMovement() {
        randomMovement(192, 384, this.x, this.y); // Varsayılan konumlar
    }

    public boolean shootTime() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime > shootInterval) {
            lastShootTime = currentTime;
            return random.nextInt(100) < shootChance;
        }
        return false;
    }

}
