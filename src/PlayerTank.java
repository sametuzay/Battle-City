public class PlayerTank extends Tank {
    public int lives;

    private boolean shielded = false;
    private long shieldEndTime = 0;
    private int shieldFrame = 0;
    private long lastShieldAnimTime = 0;
    private static final long SHIELD_ANIM_DELAY_MS = 40;

    private int starLevel = 0;
    public int maxBullets = 1;

    public PlayerTank(int x, int y, int width, int height, int speed, Direction direction) {
        super(x, y, width, height, 2, direction);
        this.lives = 3;
        activateShield(3000);
        this.lastShieldAnimTime = System.currentTimeMillis();
    }

    public void activateShield(long durationMs) {
        this.shielded = true;
        this.shieldEndTime = System.currentTimeMillis() + durationMs;
    }

    public boolean isShielded() {
        if (shielded && System.currentTimeMillis() > shieldEndTime) {
            shielded = false;
        }
        return shielded;
    }

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

    public int getShieldFrame() {
        return shieldFrame;
    }

    public void collectStar() {
        if (starLevel < 3) {
            starLevel++;
        }
        if (starLevel >= 2) {
            maxBullets = 2;
        } else {
            maxBullets = 1;
        }
    }

    public void resetStarLevel() {
        starLevel = 0;
        maxBullets = 1;
    }

    public int getStarLevel() {
        return starLevel;
    }

    @Override
    protected Bullet fireBullet() {
        int bulletX = this.x + (this.width / 2);
        int bulletY = this.y + (this.height / 2);

        int bulletSpeed = 6;
        if (starLevel >= 1) {
            bulletSpeed = 10;
        }

        boolean destroysSteel = false;
        if (starLevel >= 3) {
            destroysSteel = true;
        }

        return new Bullet(bulletX, bulletY, this.direction, false, bulletSpeed, destroysSteel);
    }
}

