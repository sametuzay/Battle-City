public class PlayerBase extends GameObject {
    protected boolean isDestroyed;

    public PlayerBase(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.isDestroyed = false;
    }

}
