public class Bullet extends GameObject {
    protected int speed;
    protected Direction direction;
    public boolean isEnemy;
    public boolean destroysSteel = false;

    public Bullet(int x, int y, Direction direction, boolean isEnemy) {
        super(x, y, 8, 8);
        this.speed = 6;
        this.direction = direction;
        this.isEnemy = isEnemy;
    }

    public Bullet(int x, int y, Direction direction, boolean isEnemy, int speed, boolean destroysSteel) {
        super(x, y, 8, 8);
        this.speed = speed;
        this.direction = direction;
        this.isEnemy = isEnemy;
        this.destroysSteel = destroysSteel;
    }

    public Direction getDirection() {
        return direction;
    }

    public void move() {
        if (direction == Direction.UP) {
            y -= speed;
        } else if (direction == Direction.DOWN) {
            y += speed;
        } else if (direction == Direction.LEFT) {
            x -= speed;
        } else if (direction == Direction.RIGHT) {
            x += speed;
        }
    }
}

