public class SteelWall extends Wall {
    private boolean shovelWall = false;

    public SteelWall(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public SteelWall(int x, int y, int width, int height, boolean shovelWall) {
        super(x, y, width, height);
        this.shovelWall = shovelWall;
    }

    public boolean isShovelWall() {
        return shovelWall;
    }
}

