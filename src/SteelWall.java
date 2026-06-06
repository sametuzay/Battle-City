public class SteelWall extends Wall {
    // Kazma (Shovel) powerup tarafından geçici olarak yerleştirilen çelik duvar mı?
    private boolean shovelWall = false;

    public SteelWall(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    // Shovel powerup'ından gelen geçici çelik duvar için constructor
    public SteelWall(int x, int y, int width, int height, boolean shovelWall) {
        super(x, y, width, height);
        this.shovelWall = shovelWall;
    }

    // Bu duvar shovel powerup'ından mı geldi?
    public boolean isShovelWall() {
        return shovelWall;
    }
}

