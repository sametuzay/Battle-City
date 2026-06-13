import java.awt.image.BufferedImage;
import java.io.File;// bunu anlatabiliyo olmam lazm
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoader {
    public static BufferedImage spriteSheet;
    public static BufferedImage[] bullets = new BufferedImage[4];
    public static BufferedImage brickWallFull;
    public static BufferedImage brickWallLeftHalf;
    public static BufferedImage brickWallRightHalf;
    public static BufferedImage brickWallTopHalf;
    public static BufferedImage brickWallBottomHalf;

    public static BufferedImage steelWall, water, bush, baseEagle, destroyedEagle;
    public static BufferedImage enemyIcon;
    public static BufferedImage miscSheet;
    public static BufferedImage ipSign;
    public static BufferedImage playerIcon;
    public static BufferedImage flagIcon;
    public static BufferedImage hudBgPattern;
    public static BufferedImage gameLogo;
    public static BufferedImage gameOverSprite;
    public static BufferedImage[] spawnStar = new BufferedImage[4];
    public static BufferedImage[] powerupSprites = new BufferedImage[6];
    public static BufferedImage[] explosionSprites = new BufferedImage[5];
    public static BufferedImage[] shieldSprites = new BufferedImage[2];

    public static BufferedImage[][] playerTank = new BufferedImage[4][2];
    public static BufferedImage[][] enemyTank = new BufferedImage[4][2];

    public static void loadImages() {
        try {
            spriteSheet = ImageIO
                    .read(new File("ImagePack/Battle City - Miscellaneous - General Sprites-transparent.png"));
            miscSheet = ImageIO.read(new File("ImagePack/Battle City - Miscellaneous - Miscellaneous.png"));
            loadPlayerTank();
            loadEnemyTank();
            loadBullets();
            loadMapSprites();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadPlayerTank() {
        playerTank[Direction.UP.ordinal()][0] = spriteSheet.getSubimage(0, 0, 16, 16);
        playerTank[Direction.UP.ordinal()][1] = spriteSheet.getSubimage(16, 0, 16, 16);

        playerTank[Direction.LEFT.ordinal()][0] = spriteSheet.getSubimage(32, 0, 16, 16);
        playerTank[Direction.LEFT.ordinal()][1] = spriteSheet.getSubimage(48, 0, 16, 16);

        playerTank[Direction.DOWN.ordinal()][0] = spriteSheet.getSubimage(64, 0, 16, 16);
        playerTank[Direction.DOWN.ordinal()][1] = spriteSheet.getSubimage(80, 0, 16, 16);

        playerTank[Direction.RIGHT.ordinal()][0] = spriteSheet.getSubimage(96, 0, 16, 16);
        playerTank[Direction.RIGHT.ordinal()][1] = spriteSheet.getSubimage(112, 0, 16, 16);
    }

    private static void loadEnemyTank() {
        enemyTank[Direction.UP.ordinal()][0] = spriteSheet.getSubimage(128, 0, 16, 16);
        enemyTank[Direction.UP.ordinal()][1] = spriteSheet.getSubimage(144, 0, 16, 16);

        enemyTank[Direction.LEFT.ordinal()][0] = spriteSheet.getSubimage(160, 0, 16, 16);
        enemyTank[Direction.LEFT.ordinal()][1] = spriteSheet.getSubimage(176, 0, 16, 16);

        enemyTank[Direction.DOWN.ordinal()][0] = spriteSheet.getSubimage(192, 0, 16, 16);
        enemyTank[Direction.DOWN.ordinal()][1] = spriteSheet.getSubimage(208, 0, 16, 16);

        enemyTank[Direction.RIGHT.ordinal()][0] = spriteSheet.getSubimage(224, 0, 16, 16);
        enemyTank[Direction.RIGHT.ordinal()][1] = spriteSheet.getSubimage(240, 0, 16, 16);
    }

    private static void loadBullets() {
        bullets[Direction.UP.ordinal()] = spriteSheet.getSubimage(323, 102, 4, 4);
        bullets[Direction.DOWN.ordinal()] = spriteSheet.getSubimage(339, 102, 4, 4);
        bullets[Direction.LEFT.ordinal()] = spriteSheet.getSubimage(330, 102, 4, 4);
        bullets[Direction.RIGHT.ordinal()] = spriteSheet.getSubimage(346, 102, 4, 4);
    }

    private static void loadMapSprites() {

        brickWallFull = spriteSheet.getSubimage(256, 0, 16, 16);
        brickWallRightHalf = spriteSheet.getSubimage(272, 0, 16, 16);
        brickWallBottomHalf = spriteSheet.getSubimage(288, 0, 16, 16);
        brickWallLeftHalf = spriteSheet.getSubimage(304, 0, 16, 16);
        brickWallTopHalf = spriteSheet.getSubimage(288, 8, 16, 16);

        steelWall = spriteSheet.getSubimage(256, 16, 16, 16);
        water = spriteSheet.getSubimage(256, 32, 16, 16);
        bush = spriteSheet.getSubimage(272, 32, 16, 16);
        baseEagle = spriteSheet.getSubimage(304, 32, 16, 16);
        destroyedEagle = spriteSheet.getSubimage(320, 32, 16, 16);

        enemyIcon = miscSheet.getSubimage(233, 245, 8, 8);
        ipSign = miscSheet.getSubimage(232, 349, 16, 8);
        playerIcon = miscSheet.getSubimage(232, 357, 8, 8);
        flagIcon = miscSheet.getSubimage(232, 397, 16, 16);
        hudBgPattern = miscSheet.getSubimage(224, 240, 8, 8);
        gameLogo = miscSheet.getSubimage(18, 16, 198, 96);
        gameOverSprite = spriteSheet.getSubimage(288, 184, 32, 16);
        spawnStar[0] = spriteSheet.getSubimage(256, 96, 16, 16);
        spawnStar[1] = spriteSheet.getSubimage(272, 96, 16, 16);
        spawnStar[2] = spriteSheet.getSubimage(288, 96, 16, 16);
        spawnStar[3] = spriteSheet.getSubimage(304, 96, 16, 16);

        powerupSprites[0] = spriteSheet.getSubimage(256, 112, 16, 16);
        powerupSprites[1] = spriteSheet.getSubimage(272, 112, 16, 16);
        powerupSprites[2] = spriteSheet.getSubimage(288, 112, 16, 16);
        powerupSprites[3] = spriteSheet.getSubimage(304, 112, 16, 16);
        powerupSprites[4] = spriteSheet.getSubimage(320, 112, 16, 16);
        powerupSprites[5] = spriteSheet.getSubimage(336, 112, 16, 16);

        explosionSprites[0] = spriteSheet.getSubimage(256, 128, 16, 16);
        explosionSprites[1] = spriteSheet.getSubimage(272, 128, 16, 16);
        explosionSprites[2] = spriteSheet.getSubimage(288, 128, 16, 16);
        explosionSprites[3] = spriteSheet.getSubimage(304, 128, 32, 32);
        explosionSprites[4] = spriteSheet.getSubimage(336, 128, 32, 32);

        shieldSprites[0] = spriteSheet.getSubimage(256, 144, 16, 16);
        shieldSprites[1] = spriteSheet.getSubimage(272, 144, 16, 16);
    }
}
