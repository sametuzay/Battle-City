import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.io.*;

public class GamePanel extends JPanel implements Runnable {
    private PlayerTank player;
    private PlayerBase playerBase;
    private Thread gameThread;
    private int currentLevel = 1;
    private final int MAX_LEVELS = 3;
    private boolean isGameOver = false;
    private boolean isVictory = false;
    private boolean isCustomMap = false;
    private String customMapPath = "";
    private int cumulativeScore = 0;
    private int enemiesKilled = 0;
    private int enemiesSpawned = 0;
    private long lastSpawnTime = 0;
    private long levelStartDelayTime = 0;
    public boolean isPaused = false;
    private GameStateListener listener;
    private List<GameObject> obstacles;
    private List<Bullet> bullets;
    private List<EnemyTank> enemies;
    private List<SpawnIndicator> spawnIndicators;
    private List<Powerup> powerups;
    private List<Explosion> explosions;
    private Difficulty difficulty = Difficulty.MEDIUM;

    private boolean enemiesFrozen = false;
    private long freezeEndTime = 0;

    private boolean shovelActive = false;
    private long shovelEndTime = 0;

    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public GamePanel(GameStateListener listener) {
        this.listener = listener;
        setBackground(Color.BLACK);
        setFocusable(true);
        setPreferredSize(new Dimension(416, 416));
        setMinimumSize(new Dimension(416, 416));
        setMaximumSize(new Dimension(416, 416));

        ImageLoader.loadImages();

        player = new PlayerTank(128, 384, 32, 32, 3, Direction.UP);
        playerBase = new PlayerBase(384, 500, 32, 32);

        obstacles = new ArrayList<>();
        bullets = new ArrayList<>();
        spawnIndicators = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();

        loadLevel(1);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (GamePanel.this) {// bütün synchronized kullanım sebeplerini öğren
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
                        upPressed = true;
                    if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
                        downPressed = true;
                    if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
                        leftPressed = true;
                    if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
                        rightPressed = true;
                    if (key == KeyEvent.VK_SPACE) {
                        // Düşman mermileri oyuncunun atış hakkını yemesin.
                        int playerBulletCount = 0;
                        for (Bullet b : bullets) {
                            if (!b.isEnemy) {
                                playerBulletCount++;
                            }
                        }
                        if (!isPaused && playerBulletCount < player.maxBullets) {
                            bullets.add(player.fireBullet());
                        }
                    }
                    if (key == KeyEvent.VK_P)
                        isPaused = !isPaused;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                synchronized (GamePanel.this) {// key released içinde neden space yok?
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
                        upPressed = false;
                    if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
                        downPressed = false;
                    if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
                        leftPressed = false;
                    if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
                        rightPressed = false;
                }
            }
        });
    }

    public void startGameThread() {// THREAD mekanizmasının nasıl çalıştığını aşama aşama anlat
        gameThread = new Thread(this);
        gameThread.start();
    }

    public synchronized void update() {
        if (isGameOver || isVictory || isPaused)
            return;
        if (System.currentTimeMillis() < levelStartDelayTime)
            return;

        boolean playerMoving = false;
        if (upPressed) {
            player.setDirection(Direction.UP);
            playerMoving = true;
            if (!checkCollision(player, Direction.UP))
                player.move();
        }
        if (downPressed) {
            player.setDirection(Direction.DOWN);
            playerMoving = true;
            if (!checkCollision(player, Direction.DOWN))
                player.move();
        }
        if (leftPressed) {
            player.setDirection(Direction.LEFT);
            playerMoving = true;
            if (!checkCollision(player, Direction.LEFT))
                player.move();
        }
        if (rightPressed) {
            player.setDirection(Direction.RIGHT);
            playerMoving = true;
            if (!checkCollision(player, Direction.RIGHT))
                player.move();
        }

        player.setMoving(playerMoving);
        player.updateAnimation();
        player.updateShieldAnimation();

        if (enemiesSpawned == 20 && enemies.isEmpty() && spawnIndicators.isEmpty()) {
            if (isCustomMap) {
                isVictory = true;
                saveScore();
            } else if (currentLevel < MAX_LEVELS) {
                cumulativeScore += enemiesKilled * 100;
                currentLevel++;
                loadLevel(currentLevel);
            } else if (currentLevel == MAX_LEVELS) {
                isVictory = true;
                saveScore();
            }
        }

        synchronized (this) {
            Iterator<SpawnIndicator> it = spawnIndicators.iterator();// bunun sebebini tam anla bi
            while (it.hasNext()) {
                SpawnIndicator si = it.next();
                if (si.isExpired()) {
                    enemies.add(new EnemyTank(si.getX(), si.getY(), si.getDifficulty(), si.getStageLevel()));
                    it.remove();
                }
            }
        }

        updateBullets();
        spawnEnemy();
        updateEnemies();
        updatePowerups();
        updateExplosions();

        if (enemiesFrozen && System.currentTimeMillis() > freezeEndTime) {
            enemiesFrozen = false;
        }

        if (shovelActive && System.currentTimeMillis() > shovelEndTime) {
            removeShovelWalls();
            shovelActive = false;
        }

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);// bunun mantuğunı tam öğrenelim.
        if (ImageLoader.spriteSheet == null)
            return;

        synchronized (this) {
            BufferedImage eagleImg = playerBase.isDestroyed ? ImageLoader.destroyedEagle : ImageLoader.baseEagle;
            g.drawImage(eagleImg, playerBase.getX(), playerBase.getY(), playerBase.getWidth(),
                    playerBase.getHeight(), this);

            for (GameObject obj : obstacles) {
                if (obj instanceof BrickWall) {
                    BrickWall brick = (BrickWall) obj;
                    if (brick.durability == 2) {
                        g.drawImage(ImageLoader.brickWallFull, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight(),
                                this);
                    } else if (brick.durability == 1) {
                        BufferedImage img = ImageLoader.brickWallRightHalf;
                        if (brick.hitDirection == Direction.UP) {
                            img = ImageLoader.brickWallTopHalf;
                        } else if (brick.hitDirection == Direction.DOWN) {
                            img = ImageLoader.brickWallBottomHalf;
                        } else if (brick.hitDirection == Direction.LEFT) {
                            img = ImageLoader.brickWallLeftHalf;
                        } else if (brick.hitDirection == Direction.RIGHT) {
                            img = ImageLoader.brickWallRightHalf;
                        }
                        g.drawImage(img, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight(), this);
                    }
                } else if (obj instanceof SteelWall) {
                    g.drawImage(ImageLoader.steelWall, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight(), this);
                } else if (obj instanceof Water) {
                    g.drawImage(ImageLoader.water, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight(), this);
                }
            }

            if (player != null) {// bunu nasıl animasyonlu yapıyo anlayalım
                BufferedImage currentTankImage = ImageLoader.playerTank[player.getDirection().ordinal()][player
                        .getAnimFrame()];
                g.drawImage(currentTankImage, player.getX(), player.getY(), player.getWidth(), player.getHeight(),
                        this);

                if (player.isShielded()) {
                    BufferedImage shieldImg = ImageLoader.shieldSprites[player.getShieldFrame()];
                    g.drawImage(shieldImg, player.getX() - 4, player.getY() - 4, player.getWidth() + 8,
                            player.getHeight() + 8, this);
                }
            }
            for (EnemyTank enemy : enemies) {// bunu nasıl animasyonlu yapıyo anlayalım.
                BufferedImage enemyImage = ImageLoader.enemyTank[enemy.getDirection().ordinal()][enemy.getAnimFrame()];
                g.drawImage(enemyImage, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), this);
            }

            for (SpawnIndicator si : spawnIndicators) {
                BufferedImage frame = ImageLoader.spawnStar[si.getFrame()];
                g.drawImage(frame, si.getX(), si.getY(), si.getWidth(), si.getHeight(), this);
            }

            for (Powerup pu : powerups) {
                if (pu.isVisible()) {
                    BufferedImage puImg = ImageLoader.powerupSprites[pu.getSpriteIndex()];
                    g.drawImage(puImg, pu.getX(), pu.getY(), pu.getWidth(), pu.getHeight(), this);
                }
            }

            for (Explosion exp : explosions) {
                BufferedImage expImg = ImageLoader.explosionSprites[exp.getFrame()];
                g.drawImage(expImg, exp.getX(), exp.getY(), exp.getWidth(), exp.getHeight(), this);
            }

            for (Bullet b : bullets) {
                BufferedImage bulletImg = ImageLoader.bullets[b.getDirection().ordinal()];
                g.drawImage(bulletImg, b.getX(), b.getY(), b.getWidth(), b.getHeight(), this);
            }

            // Battle City'de çalılar tankların üstünü örter.
            for (GameObject obj : obstacles) {
                if (obj instanceof Bush) {
                    g.drawImage(ImageLoader.bush, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight(), this);
                }
            }
            if (isPaused) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Monospaced", Font.BOLD, 40));
                g.drawString("PAUSED", 140, 200);
            }
            if (isGameOver) {
                if (ImageLoader.gameOverSprite != null) {
                    int drawW = 32 * 5;
                    int drawH = 16 * 5;
                    int drawX = (416 - drawW) / 2;
                    int drawY = (416 - drawH) / 2;
                    g.drawImage(ImageLoader.gameOverSprite, drawX, drawY, drawW, drawH, null);
                } else {
                    g.setColor(Color.RED);
                    g.setFont(new Font("Monospaced", Font.BOLD, 40));
                    g.drawString("GAME OVER", 95, 200);
                }
            }
            if (isVictory) {
                g.setColor(Color.GREEN);
                g.setFont(new Font("Monospaced", Font.BOLD, 50));
                g.drawString("VICTORY!", 95, 200);
            }
        }
    }

    @Override
    public void run() {// mantığını anlayalım
        while (gameThread != null) {
            try {
                update();
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(16);// neden burada sleep var?

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCollision(Tank tank, Direction dir) {
        int nextX = tank.getX();
        int nextY = tank.getY();

        if (dir == Direction.UP)
            nextY -= tank.speed;// bu nasıl çalışıyo anla
        if (dir == Direction.DOWN)
            nextY += tank.speed;
        if (dir == Direction.LEFT)
            nextX -= tank.speed;
        if (dir == Direction.RIGHT)
            nextX += tank.speed;

        int padding = 2;// olayını tam anla bunun
        Rectangle hitBox = new Rectangle(nextX + padding, nextY + padding, tank.getWidth() - 2 * padding,
                tank.getHeight() - 2 * padding);

        Rectangle currentBox = new Rectangle(tank.getX() + padding, tank.getY() + padding, // bu neden var?
                tank.getWidth() - 2 * padding,
                tank.getHeight() - 2 * padding);

        for (GameObject obj : obstacles) {
            if (obj instanceof Bush)
                continue;

            if (hitBox.intersects(obj.getBounds())) {
                return true;
            }
        }
        if (hitBox.intersects(playerBase.getBounds())) {
            return true;
        }

        if (tank instanceof PlayerTank) {
            for (EnemyTank enemy : enemies) {
                Rectangle enemyBox = new Rectangle(enemy.getX() + padding, enemy.getY() + padding,
                        enemy.getWidth() - 2 * padding, enemy.getHeight() - 2 * padding);
                if (hitBox.intersects(enemyBox) || currentBox.intersects(enemyBox)) {
                    return true;// bura neyi kontrol ediyor?
                }
            }
        } else if (tank instanceof EnemyTank) {
            if (player != null) {
                Rectangle playerBox = new Rectangle(player.getX() + padding, player.getY() + padding,
                        player.getWidth() - 2 * padding, player.getHeight() - 2 * padding);
                if (hitBox.intersects(playerBox) || currentBox.intersects(playerBox)) {
                    return true;
                }
            }
            for (EnemyTank otherEnemy : enemies) {
                if (tank != otherEnemy) {// burda niye != denmiş anla
                    Rectangle otherBox = new Rectangle(otherEnemy.getX() + padding, otherEnemy.getY() + padding,
                            otherEnemy.getWidth() - 2 * padding, otherEnemy.getHeight() - 2 * padding);
                    // Düşmanlar birbirini kilitlemesin diye sadece ileri hitbox'a bakıyoruz.
                    if (hitBox.intersects(otherBox)) {
                        return true;
                    }
                }
            }
        }

        if (nextX < 0 || nextX + tank.getWidth() > 416 || nextY < 0 || nextY + tank.getHeight() > 416) {
            return true;// bunu anla
        }

        return false;
    }

    private void updateBullets() {
        Iterator<Bullet> iterator = bullets.iterator();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        while (iterator.hasNext()) {
            Bullet b = iterator.next();
            if (bulletsToRemove.contains(b)) {
                iterator.remove();
                continue;
            }
            b.move();

            boolean hit = false;
            Rectangle bBox = b.getBounds();
            GameObject obstacleToRemove = null;

            if (!hit) {
                for (Bullet bullet : bullets) {// bu tam olarak neye bakıyo anla ,niye b !=bullet var?
                    if (b != bullet && b.isEnemy != bullet.isEnemy && !bulletsToRemove.contains(bullet)) {
                        if (bBox.intersects(bullet.getBounds())) {
                            hit = true;
                            bulletsToRemove.add(bullet);
                            explosions.add(new Explosion(b.getX(), b.getY(), false));
                            break;
                        }
                    }
                }
            }

            if (!playerBase.isDestroyed && bBox.intersects(playerBase.getBounds())) {
                playerBase.isDestroyed = true;
                isGameOver = true;
                hit = true;
                explosions.add(new Explosion(playerBase.getX(), playerBase.getY(), true));
                saveScore();
            }
            if (!hit && b.isEnemy && bBox.intersects(player.getBounds())) {
                if (player.isShielded()) {
                    hit = true;
                } else {
                    player.lives--;
                    if (listener != null)
                        listener.onLivesChanged(player.lives);

                    explosions.add(new Explosion(player.getX(), player.getY(), true));

                    if (player.lives <= 0) {
                        isGameOver = true;
                        saveScore();
                    } else {
                        player.x = 128;
                        player.y = 384;
                        player.resetStarLevel();
                        player.activateShield(3000);
                    }
                    hit = true;
                }
            }
            if (!hit && !b.isEnemy) {
                Iterator<EnemyTank> enemyIt = enemies.iterator();
                while (enemyIt.hasNext()) {
                    EnemyTank enemy = enemyIt.next();
                    if (b.getBounds().intersects(enemy.getBounds())) {
                        enemyIt.remove();
                        enemiesKilled++;
                        int currentScore = getScore();
                        if (listener != null)
                            listener.onScoreChanged(currentScore);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY(), true));
                        trySpawnPowerup(enemy.getX(), enemy.getY());
                        hit = true;
                        break;
                    }
                }

            }

            if (b.x < 0 || b.x > 416 || b.y < 0 || b.y > 416) {
                hit = true;
                explosions.add(new Explosion(b.getX(), b.getY(), false));
            }
            if (!hit) {
                for (GameObject obj : obstacles) {
                    if (obj instanceof Water || obj instanceof Bush)
                        continue;

                    if (b.getBounds().intersects(obj.getBounds())) {
                        hit = true;
                        explosions.add(new Explosion(b.getX(), b.getY(), false));

                        if (obj instanceof BrickWall) {
                            BrickWall brick = (BrickWall) obj;
                            brick.durability--;
                            if (brick.durability == 1) {
                                brick.hitDirection = b.getDirection();
                            }

                            if (brick.durability <= 0) {
                                obstacleToRemove = obj;
                            }
                        } else if (obj instanceof SteelWall) {
                            if (b.destroysSteel) {
                                obstacleToRemove = obj;
                            }
                        }
                        break;
                    }
                }
            }
            if (obstacleToRemove != null) {
                obstacles.remove(obstacleToRemove);
            }
            if (hit) {
                iterator.remove();
            }
        }
    }

    private void updateEnemies() {
        if (enemiesFrozen) {
            return;
        }
        Random rng = new Random();
        int baseX = playerBase.getX();
        int baseY = playerBase.getY();

        for (EnemyTank enemy : enemies) {
            if (checkCollision(enemy, enemy.direction)) {
                boolean pathFound = false;
                int attempts = 0;
                while (!pathFound && attempts < 4) {
                    enemy.randomMovement(baseX, baseY, player.getX(), player.getY());
                    if (!checkCollision(enemy, enemy.direction)) {
                        pathFound = true;
                    }
                    attempts++;
                }
                enemy.setMoving(false);
            } else {
                enemy.move();
                enemy.setMoving(true);
                if (rng.nextInt(100) < 1) {// bu neyi anlatıyo anla
                    enemy.randomMovement(baseX, baseY, player.getX(), player.getY());
                }
            }

            enemy.updateAnimation();

            if (enemy.shootTime())
                bullets.add(enemy.fireBullet());
        }
    }

    private void updateExplosions() {// bu nasıl çalışıyo anla
        if (explosions == null) {
            return;
        }
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion exp = it.next();
            exp.update();
            if (exp.isExpired()) {
                it.remove();
            }
        }
    }

    private void updatePowerups() {
        if (powerups == null) {
            return;
        }
        Iterator<Powerup> it = powerups.iterator();
        while (it.hasNext()) {
            Powerup pu = it.next();
            if (player.getBounds().intersects(pu.getBounds())) {
                applyPowerup(pu.getType());
                it.remove();
            } else if (pu.isExpired()) {
                it.remove();
            }
        }
    }

    private void trySpawnPowerup(int x, int y) {// nasıl çalışıyor anla
        Random rng = new Random();
        if (rng.nextInt(100) < 40) {
            PowerupType[] types = PowerupType.values();
            PowerupType randomType = types[rng.nextInt(types.length)];
            powerups.add(new Powerup(x, y, randomType));
        }
    }

    private void applyPowerup(PowerupType type) {
        if (type == PowerupType.SHIELD) {
            player.activateShield(8000);
        } else if (type == PowerupType.FREEZE) {
            enemiesFrozen = true;// freezin nasıl başladığını anla
            freezeEndTime = System.currentTimeMillis() + 6000;
        } else if (type == PowerupType.SHOVEL) {
            applyShovel();
        } else if (type == PowerupType.STAR) {
            player.collectStar();
        } else if (type == PowerupType.GRENADE) {
            int killedByGrenade = enemies.size();
            enemies.clear();
            enemiesKilled += killedByGrenade;
            if (listener != null) {
                listener.onScoreChanged(getScore());
            }
        } else if (type == PowerupType.LIFE) {
            player.lives++;
            if (listener != null) {
                listener.onLivesChanged(player.lives);
            }
        }
    }

    private void applyShovel() {// bunun çalışmasını anlat
        int baseX = playerBase.getX();
        int baseY = playerBase.getY();
        int[][] positions = {
                { baseX - 16, baseY - 16 }, { baseX, baseY - 16 }, { baseX + 16, baseY - 16 },
                { baseX + 32, baseY - 16 },
                { baseX - 16, baseY }, { baseX + 32, baseY },
                { baseX - 16, baseY + 16 }, { baseX + 32, baseY + 16 }
        };
        Iterator<GameObject> it = obstacles.iterator();
        while (it.hasNext()) {
            GameObject obj = it.next();
            if (obj instanceof BrickWall) {// tam olarak napıyo bu anla
                for (int[] pos : positions) {
                    if (obj.getX() == pos[0] && obj.getY() == pos[1]) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        for (int[] pos : positions) {
            obstacles.add(new SteelWall(pos[0], pos[1], 16, 16, true));
        }
        shovelActive = true;
        shovelEndTime = System.currentTimeMillis() + 15000;
    }

    private void removeShovelWalls() {// bunu da anla
        Iterator<GameObject> it = obstacles.iterator();
        while (it.hasNext()) {
            GameObject obj = it.next();
            if (obj instanceof SteelWall) {
                SteelWall steel = (SteelWall) obj;
                if (steel.isShovelWall()) {
                    it.remove();
                }
            }
        }
    }

    private void spawnEnemy() {
        if (enemies.size() + spawnIndicators.size() < 3 && enemiesSpawned < 20) {
            long currentTime = System.currentTimeMillis();

            int baseSpawnInterval = 2000;
            if (difficulty == Difficulty.EASY) {
                baseSpawnInterval = 3000;
            } else if (difficulty == Difficulty.MEDIUM) {
                baseSpawnInterval = 2000;
            } else if (difficulty == Difficulty.HARD) {
                baseSpawnInterval = 1500;
            }

            int stageBonus = (currentLevel - 1) * 250;
            int finalSpawnInterval = Math.max(800, baseSpawnInterval - stageBonus);// bunu anla neden math kullanmış

            if (currentTime - lastSpawnTime >= finalSpawnInterval) {
                lastSpawnTime = currentTime;

                int[] spawnX = { 0, 192, 384 };
                int randomX = spawnX[new Random().nextInt(3)];

                spawnIndicators.add(new SpawnIndicator(randomX, 0, difficulty, currentLevel));
                enemiesSpawned++;

            }
        }
    }

    public synchronized void startNewGame(Difficulty selectedDifficulty) {// neden özellikle bu synch
        this.difficulty = selectedDifficulty;
        this.player.lives = 3;
        this.currentLevel = 1;
        this.isGameOver = false;
        this.isVictory = false;
        this.cumulativeScore = 0;
        this.isCustomMap = false;
        loadLevel(currentLevel);
        if (listener != null) {
            listener.onLivesChanged(this.player.lives);
            listener.onLevelChanged(this.currentLevel);
            listener.onScoreChanged(0);
        }
    }

    public synchronized void startCustomGame(Difficulty selectedDifficulty, String customMapPath) {
        this.difficulty = selectedDifficulty;
        this.player.lives = 3;
        this.currentLevel = 1;
        this.isGameOver = false;
        this.cumulativeScore = 0;
        this.isCustomMap = true;
        this.customMapPath = customMapPath;
        loadLevel(-1);// bunun amacı ne
        if (listener != null) {
            listener.onLivesChanged(this.player.lives);
            listener.onLevelChanged(this.currentLevel);
            listener.onScoreChanged(0);
        }
    }

    private synchronized void loadLevel(int level) {// neden bütün listler tekrar yapılıyo
        obstacles.clear();
        bullets.clear();
        enemiesKilled = 0;
        enemiesSpawned = 0;
        levelStartDelayTime = System.currentTimeMillis() + 1000;

        if (enemies == null)
            enemies = new ArrayList<>();
        enemies.clear();

        if (spawnIndicators == null)
            spawnIndicators = new ArrayList<>();
        spawnIndicators.clear();

        if (powerups == null)
            powerups = new ArrayList<>();
        powerups.clear();

        if (explosions == null)
            explosions = new ArrayList<>();
        explosions.clear();

        isVictory = false;

        playerBase.isDestroyed = false;

        enemiesFrozen = false;
        shovelActive = false;
        player.resetStarLevel();

        if (listener != null) {
            if (isCustomMap) {
                listener.onLevelChanged(1);
            } else {
                listener.onLevelChanged(currentLevel);
            }
        }
        player.x = 128;
        player.y = 384;
        player.setDirection(Direction.UP);
        player.activateShield(3000);
        String currentMap;
        if (isCustomMap) {
            currentMap = customMapPath;
        } else {
            currentMap = "map" + level + ".txt";
        }
        MapLoader.loadMap(currentMap, obstacles, playerBase);
    }

    public int getScore() {
        return cumulativeScore + (enemiesKilled * 100);
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public PlayerTank getPlayer() {
        return player;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCustomMap() {
        return isCustomMap;
    }

    public String getCustomMapPath() {
        return customMapPath;
    }

    public synchronized void stopGame() {
        this.isGameOver = true;
        this.isPaused = false;
        if (bullets != null) {
            bullets.clear();
        }
        if (enemies != null) {
            enemies.clear();
        }
        if (spawnIndicators != null) {
            spawnIndicators.clear();
        }
        if (powerups != null) {
            powerups.clear();
        }
        if (explosions != null) {
            explosions.clear();
        }
    }

    private void saveScore() {// neden thread var bunda ayrı
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SwingUtilities.invokeLater(new Runnable() {// bu ne işe yarıyor?
                    @Override
                    public void run() {
                        String name = JOptionPane.showInputDialog(GamePanel.this, "Game Over! Enter your name:", "Save",
                                JOptionPane.PLAIN_MESSAGE);
                        if (name != null && !name.isEmpty()) {
                            try (FileWriter fw = new FileWriter("scores.csv", true)) {// filewriter ne anla
                                fw.write(name + "," + getScore() + "\n");
                            } catch (IOException e) {
                                System.out.println("File couldn't be written.");
                            }
                        }
                        if (listener != null) {
                            listener.onGameOver();// bu ne oluyor tam?
                        }
                    }
                });
            }
        }).start();// neden burda start
    }

}
