public class Powerup extends GameObject {
    private PowerupType type;
    private long spawnTime;

    private static final long LIFETIME_MS = 30000;

    public Powerup(int x, int y, PowerupType type) {
        super(x, y, 32, 32);
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }

    public PowerupType getType() {
        return type;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > LIFETIME_MS;
    }

    public boolean isVisible() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        if (elapsed > LIFETIME_MS - 5000) {
            return (elapsed / 100) % 2 == 0;
        }
        return (elapsed / 200) % 2 == 0;
    }

    public int getSpriteIndex() {
        if (type == PowerupType.SHIELD) {
            return 0;
        } else if (type == PowerupType.FREEZE) {
            return 1;
        } else if (type == PowerupType.SHOVEL) {
            return 2;
        } else if (type == PowerupType.STAR) {
            return 3;
        } else if (type == PowerupType.GRENADE) {
            return 4;
        } else if (type == PowerupType.LIFE) {
            return 5;
        }
        return 0;
    }
}
