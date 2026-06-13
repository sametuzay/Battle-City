public abstract class Tank extends GameObject {
    protected int speed;
    protected Direction direction;

    protected boolean isMoving = false;
    protected int animFrame = 0;
    protected long lastAnimTime = 0;
    protected static final long ANIM_DELAY_MS = 60;

    public Tank(int x, int y, int width, int height, int speed, Direction currentDirection) {
        super(x, y, width, height);
        this.speed = speed;
        this.direction = currentDirection;
        this.lastAnimTime = System.currentTimeMillis();
    }

    protected abstract Bullet fireBullet();

    public void move() {
        if (direction == Direction.UP) {
            y -= speed;// neden y x ten speed çıkartılıyo onu anlat
        } else if (direction == Direction.DOWN) {
            y += speed;
        } else if (direction == Direction.LEFT) {
            x -= speed;
        } else if (direction == Direction.RIGHT) {
            x += speed;
        }
    }

    public void updateAnimation() {// bunu anla
        if (isMoving) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAnimTime >= ANIM_DELAY_MS) {
                if (animFrame == 0) {
                    animFrame = 1;
                } else {
                    animFrame = 0;
                }
                lastAnimTime = currentTime;
            }
        }
    }

    public void setDirection(Direction dir) {
        this.direction = dir;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;
        if (!moving) {
            this.animFrame = 0;
        }
    }

    public int getAnimFrame() {
        return animFrame;
    }
}
