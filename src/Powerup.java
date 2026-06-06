// Haritada beliren ve oyuncu tarafından toplanabilen powerup nesnesi
public class Powerup extends GameObject {
    private PowerupType type;
    private long spawnTime;

    // Powerup 30 saniye sonra kendiliğinden yok olur
    private static final long LIFETIME_MS = 30000;

    public Powerup(int x, int y, PowerupType type) {
        super(x, y, 32, 32);
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }

    // Powerup'ın türünü döndürür
    public PowerupType getType() {
        return type;
    }

    // Powerup'ın ömrü dolmuş mu? (30 saniye sonra yok olur)
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > LIFETIME_MS;
    }

    // Yanıp sönme efekti için görünürlük durumu (her 200 ms'de bir değişir)
    public boolean isVisible() {
        long elapsed = System.currentTimeMillis() - spawnTime;
        // Son 5 saniyede daha hızlı yanıp söner (her 100 ms'de bir)
        if (elapsed > LIFETIME_MS - 5000) {
            return (elapsed / 100) % 2 == 0;
        }
        return (elapsed / 200) % 2 == 0;
    }

    // Powerup'ın spritesheet'teki x koordinatını döndürür (y=112 satırı)
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
