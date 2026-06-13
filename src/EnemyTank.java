import java.util.Random;

public class EnemyTank extends Tank {
    private Random random;
    private long lastShootTime;
    private int shootInterval;
    private int shootChance;
    private int bulletSpeed = 6;

    public EnemyTank(int x, int y, Difficulty difficulty, int stageLevel) {
        super(x, y, 32, 32, 1, Direction.DOWN);

        int baseSpeed = 1;
        int baseShootInterval = 600;
        int baseShootChance = 50;

        if (difficulty == Difficulty.EASY) {
            baseSpeed = 1;
            baseShootInterval = 800;
            baseShootChance = 30;
        } else if (difficulty == Difficulty.MEDIUM) {
            baseSpeed = 1;
            baseShootInterval = 600;
            baseShootChance = 50;
        } else if (difficulty == Difficulty.HARD) {
            baseSpeed = 2;
            baseShootInterval = 450;
            baseShootChance = 65;
        }

        int stageBonus = stageLevel - 1;

        int speedBonus = 0;
        if (stageBonus > 0) {
            speedBonus = 1;
        }
        this.speed = Math.min(2, baseSpeed + speedBonus);

        this.shootInterval = Math.max(250, baseShootInterval - (stageBonus * 75));
        this.shootChance = Math.min(85, baseShootChance + (stageBonus * 10));

        this.random = new Random();
        this.lastShootTime = System.currentTimeMillis();
    }

    @Override
    protected Bullet fireBullet() {
        int bulletX = this.x + (this.width / 2);
        int bulletY = this.y + (this.height / 2);
        return new Bullet(bulletX, bulletY, this.direction, true, this.bulletSpeed, false);
    }

    public void randomMovement(int baseX, int baseY, int playerX, int playerY) {
        int choice = random.nextInt(100);// bunu öğren kesin

        if (choice < 70) {
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

    public void randomMovement(int baseX, int baseY) {
        randomMovement(baseX, baseY, this.x, this.y);
    }

    public void randomMovement() {
        randomMovement(192, 384, this.x, this.y);
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
