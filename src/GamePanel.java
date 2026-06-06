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
    private boolean isVictory = false; // Zafer ekranı bayrağı
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
    private List<Explosion> explosions; // Aktif patlamalar listesi
    private Difficulty difficulty = Difficulty.MEDIUM;

    // Saat (Freeze) powerup: Düşmanlar dondurulmuş mu ve ne zamana kadar?
    private boolean enemiesFrozen = false;
    private long freezeEndTime = 0;

    // Kazma (Shovel) powerup: Üssün etrafı geçici çelik yapıldı mı?
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
                synchronized (GamePanel.this) {
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
                        // Sadece oyuncuya ait aktif mermileri sayıyoruz ki düşman mermileri oyuncuyu
                        // engellemesin
                        int playerBulletCount = 0;
                        for (Bullet b : bullets) {
                            if (!b.isEnemy) {
                                playerBulletCount++;
                            }
                        }
                        // Maksimum mermi sayısı: Yıldız powerup aktifken 2, değilse 1
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
                synchronized (GamePanel.this) {
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

    public void startGameThread() {
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
        player.updateShieldAnimation(); // Kalkan animasyonunu güncelle

        if (enemiesSpawned == 20 && enemies.isEmpty() && spawnIndicators.isEmpty()) {
            if (isCustomMap) {
                isVictory = true;
                saveScore();
            } else if (currentLevel < MAX_LEVELS) {
                cumulativeScore += enemiesKilled * 100;
                currentLevel++;
                loadLevel(currentLevel);
            } else if (currentLevel == MAX_LEVELS) {
                // Son seviye tamamlandı, zafer!
                isVictory = true;
                saveScore();
            }
        }

        // Doğuş göstergelerini güncelle ve süresi dolanları düşmana dönüştür
        synchronized (this) {
            Iterator<SpawnIndicator> it = spawnIndicators.iterator();
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
        updateExplosions(); // Patlamaları güncelle

        // Saat powerup süresi dolmuşsa düşmanları tekrar hareket ettir
        if (enemiesFrozen && System.currentTimeMillis() > freezeEndTime) {
            enemiesFrozen = false;
        }

        // Kazma powerup süresi dolmuşsa üssün etrafındaki çelik duvarları kaldır
        if (shovelActive && System.currentTimeMillis() > shovelEndTime) {
            removeShovelWalls();
            shovelActive = false;
        }

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (ImageLoader.spriteSheet == null)
            return;

        synchronized (this) {
            BufferedImage eagleImg = playerBase.isDestroyed ? ImageLoader.destroyedEagle : ImageLoader.baseEagle;
            g.drawImage(eagleImg, playerBase.getX(), playerBase.getY(), playerBase.getWidth(),
                    playerBase.getHeight(), this);

            // Çalı (Bush) dışındaki engelleri tankların altında kalması için önce çiziyoruz
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

            // Oyuncu Tankı çizimi
            if (player != null) {
                BufferedImage currentTankImage = ImageLoader.playerTank[player.getDirection().ordinal()][player
                        .getAnimFrame()];
                g.drawImage(currentTankImage, player.getX(), player.getY(), player.getWidth(), player.getHeight(),
                        this);

                // Oyuncu kalkanı/doğuş koruması çizimi (NES Sprites) - Mükemmel ortalama için
                // genişletildi
                if (player.isShielded()) {
                    BufferedImage shieldImg = ImageLoader.shieldSprites[player.getShieldFrame()];
                    g.drawImage(shieldImg, player.getX() - 4, player.getY() - 4, player.getWidth() + 8,
                            player.getHeight() + 8, this);
                }
            }
            // Düşman Tankları çizimi
            for (EnemyTank enemy : enemies) {
                BufferedImage enemyImage = ImageLoader.enemyTank[enemy.getDirection().ordinal()][enemy.getAnimFrame()];
                g.drawImage(enemyImage, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), this);
            }

            // Düşman Doğuş Yıldızlarını çizdir
            for (SpawnIndicator si : spawnIndicators) {
                BufferedImage frame = ImageLoader.spawnStar[si.getFrame()];
                g.drawImage(frame, si.getX(), si.getY(), si.getWidth(), si.getHeight(), this);
            }

            // Powerup'ları çizdir (yanıp sönerek)
            for (Powerup pu : powerups) {
                if (pu.isVisible()) {
                    BufferedImage puImg = ImageLoader.powerupSprites[pu.getSpriteIndex()];
                    g.drawImage(puImg, pu.getX(), pu.getY(), pu.getWidth(), pu.getHeight(), this);
                }
            }

            // Patlamaları çizdir
            for (Explosion exp : explosions) {
                BufferedImage expImg = ImageLoader.explosionSprites[exp.getFrame()];
                g.drawImage(expImg, exp.getX(), exp.getY(), exp.getWidth(), exp.getHeight(), this);
            }

            // Mermileri çizdir
            for (Bullet b : bullets) {
                BufferedImage bulletImg = ImageLoader.bullets[b.getDirection().ordinal()];
                g.drawImage(bulletImg, b.getX(), b.getY(), b.getWidth(), b.getHeight(), this);
            }

            // Orijinal oyundaki gibi çalıları (Bush) tankların üstünü örtmesi için en son
            // çiziyoruz
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
    public void run() {
        while (gameThread != null) {
            try {
                update();
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(16);// 1000/60'Tan geliyor, 16 ms, 60fps.

            } catch (InterruptedException e) {// sleepteyken bozulursa
                e.printStackTrace();
            }
        }
    }

    private boolean checkCollision(Tank tank, Direction dir) {
        int nextX = tank.getX();
        int nextY = tank.getY();

        if (dir == Direction.UP)
            nextY -= tank.speed;
        if (dir == Direction.DOWN)
            nextY += tank.speed;
        if (dir == Direction.LEFT)
            nextX -= tank.speed;
        if (dir == Direction.RIGHT)
            nextX += tank.speed;

        int padding = 2;
        Rectangle hitBox = new Rectangle(nextX + padding, nextY + padding, tank.getWidth() - 2 * padding,
                tank.getHeight() - 2 * padding);

        // Anlık çakışmaları da engellemek için mevcut tankın kendi sınır kutusu
        Rectangle currentBox = new Rectangle(tank.getX() + padding, tank.getY() + padding,
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

        // --- Tank-Tank Çarpışma Kontrolleri ---
        if (tank instanceof PlayerTank) {
            // Oyuncu düşman tanklarına çarpıyor mu?
            for (EnemyTank enemy : enemies) {
                Rectangle enemyBox = new Rectangle(enemy.getX() + padding, enemy.getY() + padding,
                        enemy.getWidth() - 2 * padding, enemy.getHeight() - 2 * padding);
                if (hitBox.intersects(enemyBox) || currentBox.intersects(enemyBox)) {
                    return true;
                }
            }
        } else if (tank instanceof EnemyTank) {
            // Düşman oyuncuya çarpıyor mu?
            if (player != null) {
                Rectangle playerBox = new Rectangle(player.getX() + padding, player.getY() + padding,
                        player.getWidth() - 2 * padding, player.getHeight() - 2 * padding);
                if (hitBox.intersects(playerBox) || currentBox.intersects(playerBox)) {
                    return true;
                }
            }
            // Düşman diğer düşman tanklarına çarpıyor mu?
            for (EnemyTank otherEnemy : enemies) {
                if (tank != otherEnemy) {
                    Rectangle otherBox = new Rectangle(otherEnemy.getX() + padding, otherEnemy.getY() + padding,
                            otherEnemy.getWidth() - 2 * padding, otherEnemy.getHeight() - 2 * padding);
                    // Düşmanlar birbirini kilitlemesin diye sadece gideceği yönü (hitBox) kontrol
                    // ediyoruz
                    if (hitBox.intersects(otherBox)) {
                        return true;
                    }
                }
            }
        }

        if (nextX < 0 || nextX + tank.getWidth() > 416 || nextY < 0 || nextY + tank.getHeight() > 416) {
            return true;
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
                for (Bullet bullet : bullets) {
                    if (b != bullet && b.isEnemy != bullet.isEnemy && !bulletsToRemove.contains(bullet)) {
                        if (bBox.intersects(bullet.getBounds())) {
                            hit = true;
                            bulletsToRemove.add(bullet);
                            // Mermilerin havada çarpışması (küçük patlama)
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
                // Kartal yok edilince büyük patlama oluşur
                explosions.add(new Explosion(playerBase.getX(), playerBase.getY(), true));
                saveScore();
            }
            if (!hit && b.isEnemy && bBox.intersects(player.getBounds())) {// oyuncunun vurulma mekaniği
                if (player.isShielded()) {
                    // Oyuncu kalkanlıysa zarar görmez, mermi patlar/yok olur
                    hit = true;
                } else {
                    player.lives--;
                    if (listener != null)
                        listener.onLivesChanged(player.lives);

                    // Oyuncu vurulunca büyük patlama oluşur
                    explosions.add(new Explosion(player.getX(), player.getY(), true));

                    if (player.lives <= 0) {
                        isGameOver = true;
                        saveScore();
                    } else {
                        player.x = 128;
                        player.y = 384;
                        player.resetStarLevel(); // Oyuncu ölünce yıldız seviyesi sıfırlanır
                        player.activateShield(3000); // Yeniden doğunca 3 saniye kalkan verilir
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
                        // Düşman yok edilince büyük patlama oluşur
                        explosions.add(new Explosion(enemy.getX(), enemy.getY(), true));
                        // %40 ihtimalle düşman öldürülünce powerup bırak
                        trySpawnPowerup(enemy.getX(), enemy.getY());
                        hit = true;
                        break;
                    }
                }

            }

            if (b.x < 0 || b.x > 416 || b.y < 0 || b.y > 416) {// harita sınırı kontrolü
                hit = true;
                // Duvara/Sınıra çarpınca küçük patlama
                explosions.add(new Explosion(b.getX(), b.getY(), false));
            }
            if (!hit) {
                for (GameObject obj : obstacles) {
                    if (obj instanceof Water || obj instanceof Bush)
                        continue;

                    if (b.getBounds().intersects(obj.getBounds())) {
                        hit = true;
                        // Engele çarptığında küçük patlama
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
                            // Mermi çelik kırıcı güce sahipse çelik duvarı yok et
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
        // Saat (Freeze) powerup aktifken düşmanlar hareket etmez ve ateş etmez
        if (enemiesFrozen) {
            return;
        }
        Random rng = new Random();
        int baseX = playerBase.getX();
        int baseY = playerBase.getY();

        for (EnemyTank enemy : enemies) {
            if (checkCollision(enemy, enemy.direction)) {
                // Çarpışma oldu, açık bir yön bulana kadar dene (Maksimum 4 deneme)
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
                // Çarpışma yoksa hareket et
                enemy.move();
                enemy.setMoving(true);
                // Düz yolda giderken ara sıra (%1 şansla) doğal hedef seçimi yapsın
                // (yalpalamayı önlemek için düşük tutulur)
                if (rng.nextInt(100) < 1) {
                    enemy.randomMovement(baseX, baseY, player.getX(), player.getY());
                }
            }

            enemy.updateAnimation();

            if (enemy.shootTime())
                bullets.add(enemy.fireBullet());
        }
    }

    // Patlamaları güncelle: animasyon karelerini ilerlet ve süresi dolanları sil
    private void updateExplosions() {
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

    // Powerup'ları güncelle: oyuncuyla çakışma kontrolü ve süresi dolanıları
    // temizle
    private void updatePowerups() {
        if (powerups == null) {
            return;
        }
        Iterator<Powerup> it = powerups.iterator();
        while (it.hasNext()) {
            Powerup pu = it.next();
            // Oyuncu powerup'a değdi mi?
            if (player.getBounds().intersects(pu.getBounds())) {
                applyPowerup(pu.getType());
                it.remove();
            } else if (pu.isExpired()) {
                // 30 saniye doldu, powerup yok olur
                it.remove();
            }
        }
    }

    // Düşman öldürülünce %40 ihtimalle bir powerup bırak
    private void trySpawnPowerup(int x, int y) {
        Random rng = new Random();
        if (rng.nextInt(100) < 40) {
            PowerupType[] types = PowerupType.values();
            PowerupType randomType = types[rng.nextInt(types.length)];
            powerups.add(new Powerup(x, y, randomType));
        }
    }

    // Toplanan powerup'u uygula
    private void applyPowerup(PowerupType type) {
        if (type == PowerupType.SHIELD) {
            // 8 saniye kalkan ver
            player.activateShield(8000);
        } else if (type == PowerupType.FREEZE) {
            // Tüm düşmanları 6 saniye dondur
            enemiesFrozen = true;
            freezeEndTime = System.currentTimeMillis() + 6000;
        } else if (type == PowerupType.SHOVEL) {
            // Üssün etrafını 15 saniye çeliğe çevir
            applyShovel();
        } else if (type == PowerupType.STAR) {
            // Yıldız seviyesini kalıcı olarak yükseltir (max 3)
            player.collectStar();
        } else if (type == PowerupType.GRENADE) {
            // Ekrandaki tüm düşmanları yok et
            int killedByGrenade = enemies.size();
            enemies.clear();
            enemiesKilled += killedByGrenade;
            if (listener != null) {
                listener.onScoreChanged(getScore());
            }
        } else if (type == PowerupType.LIFE) {
            // +1 can
            player.lives++;
            if (listener != null) {
                listener.onLivesChanged(player.lives);
            }
        }
    }

    // Üssün etrafındaki tuğlaları geçici olarak çeliğe dönüştür
    private void applyShovel() {
        int baseX = playerBase.getX();
        int baseY = playerBase.getY();
        // Üss çevresindeki koordinatları tanımlıyoruz (16px blok boyutunda, üs
        // 32x32'dir)
        int[][] positions = {
                { baseX - 16, baseY - 16 }, { baseX, baseY - 16 }, { baseX + 16, baseY - 16 },
                { baseX + 32, baseY - 16 },
                { baseX - 16, baseY }, { baseX + 32, baseY },
                { baseX - 16, baseY + 16 }, { baseX + 32, baseY + 16 }
        };
        // Mevcut tuğlaları sil, yerine çelik koy
        Iterator<GameObject> it = obstacles.iterator();
        while (it.hasNext()) {
            GameObject obj = it.next();
            if (obj instanceof BrickWall) {
                for (int[] pos : positions) {
                    if (obj.getX() == pos[0] && obj.getY() == pos[1]) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        for (int[] pos : positions) {
            obstacles.add(new SteelWall(pos[0], pos[1], 16, 16, true)); // shovelWall=true
        }
        shovelActive = true;
        shovelEndTime = System.currentTimeMillis() + 15000; // 15 saniye
    }

    // Shovel süresi dolunca eklenen geçici çelik duvarları kaldır
    private void removeShovelWalls() {
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

            // Seçilen zorluk derecesine göre temel doğuş sıklığı
            int baseSpawnInterval = 2000;
            if (difficulty == Difficulty.EASY) {
                baseSpawnInterval = 3000; // 3 saniye
            } else if (difficulty == Difficulty.MEDIUM) {
                baseSpawnInterval = 2000; // 2 saniye
            } else if (difficulty == Difficulty.HARD) {
                baseSpawnInterval = 1500; // 1.5 saniye
            }

            // Bölüm ilerledikçe doğuş gecikmesini azaltıyoruz (düşmanların daha sık doğması
            // için)
            int stageBonus = (currentLevel - 1) * 250; // Her stage için 250 ms düşüş
            int finalSpawnInterval = Math.max(800, baseSpawnInterval - stageBonus); // En düşük 800 ms olabilir

            if (currentTime - lastSpawnTime >= finalSpawnInterval) {
                lastSpawnTime = currentTime;

                int[] spawnX = { 0, 192, 384 };
                int randomX = spawnX[new Random().nextInt(3)];

                spawnIndicators.add(new SpawnIndicator(randomX, 0, difficulty, currentLevel));
                enemiesSpawned++;

            }
        }
    }

    public synchronized void startNewGame(Difficulty selectedDifficulty) {
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
        loadLevel(-1);
        if (listener != null) {
            listener.onLivesChanged(this.player.lives);
            listener.onLevelChanged(this.currentLevel);
            listener.onScoreChanged(0);
        }
    }

    private synchronized void loadLevel(int level) {
        obstacles.clear();
        bullets.clear();
        enemiesKilled = 0;
        enemiesSpawned = 0;
        levelStartDelayTime = System.currentTimeMillis() + 1000; // Yeni seviye yüklenince 1 saniye bekletiyoruz

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

        isVictory = false; // Seviye yüklenirken sıfırla

        // Üssün hasar durumunu sıfırla
        playerBase.isDestroyed = false;

        // Powerup yan etkilerini sıfırla
        enemiesFrozen = false;
        shovelActive = false;
        player.resetStarLevel(); // Bölüm başlayınca yıldız seviyesi sıfırlanır

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
        player.activateShield(3000); // Bölüm başlayınca 3 saniye kalkan verilir
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

    private void saveScore() {
        // Swing Event Thread'ini kilitlemeden 2 saniye beklemek için yeni bir iş
        // parçacığı (Thread) başlatıyoruz
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // 2 saniye bekletiyoruz
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Gecikme bittikten sonra dialogu Swing EDT üzerinde açıyoruz
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String name = JOptionPane.showInputDialog(GamePanel.this, "Game Over! Enter your name:", "Save",
                                JOptionPane.PLAIN_MESSAGE);
                        if (name != null && !name.isEmpty()) {
                            try (FileWriter fw = new FileWriter("scores.csv", true)) { // true = dosyanın sonuna ekle
                                fw.write(name + "," + getScore() + "\n");
                            } catch (IOException e) {
                                System.out.println("File couldn't be written.");
                            }
                        }
                        if (listener != null) {
                            listener.onGameOver();
                        }
                    }
                });
            }
        }).start();
    }

}
