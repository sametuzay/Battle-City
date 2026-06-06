import java.awt.Rectangle;

public class BrickWall extends Wall {
    protected boolean isDestroyed;
    public int durability;
    public Direction hitDirection;

    public BrickWall(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.isDestroyed = false;
        this.durability = 2;
        this.hitDirection = null;
    }

    @Override
    public Rectangle getBounds() {
        if (durability == 1 && hitDirection != null) {
            if (hitDirection == Direction.UP) {
                // Aşağısı kırıldı, üst yarısı kaldı
                return new Rectangle(x, y, width, height / 2);
            } else if (hitDirection == Direction.DOWN) {
                // Yukarısı kırıldı, alt yarısı kaldı
                return new Rectangle(x, y + height / 2, width, height / 2);
            } else if (hitDirection == Direction.LEFT) {
                // Sağ tarafı kırıldı, sol yarısı kaldı
                return new Rectangle(x, y, width / 2, height);
            } else if (hitDirection == Direction.RIGHT) {
                // Sol tarafı kırıldı, sağ yarısı kaldı
                return new Rectangle(x + width / 2, y, width / 2, height);
            }
        }
        return super.getBounds();
    }
}
