import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class GameFrame extends JFrame implements GameStateListener {

    private GamePanel gamePanel;
    private HUDPanel infoPanel;
    private JMenuBar menuBar;
    private JPanel gameContainer;
    private MainMenuPanel mainMenuPanel;

    public GameFrame() {
        setTitle("Battle City - Tank 1990");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenu helpMenu = new JMenu("Help");

        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem loadCustomMap = new JMenuItem("Load Custom Map");
        JMenuItem mapEditor = new JMenuItem("Map Editor");
        JMenuItem backToMenu = new JMenuItem("Back to Main Menu");
        JMenuItem options = new JMenuItem("Options");
        JMenuItem highScores = new JMenuItem("High Scores");
        JMenuItem help = new JMenuItem("Help");
        JMenuItem about = new JMenuItem("About");
        JMenuItem exitItem = new JMenuItem("Exit");

        gameMenu.add(newGame);
        gameMenu.add(loadCustomMap);
        gameMenu.add(mapEditor);
        gameMenu.addSeparator();
        gameMenu.add(backToMenu);
        gameMenu.add(options);
        gameMenu.add(highScores);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        helpMenu.add(help);
        helpMenu.add(about);

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);

        infoPanel = new HUDPanel();
        gamePanel = new GamePanel(this);

        gameContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (ImageLoader.hudBgPattern != null) {
                    g.drawImage(ImageLoader.hudBgPattern, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        gameContainer.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 16));
        gameContainer.add(gamePanel);

        showMainMenu();

        pack();
        setLocationRelativeTo(null);
        this.setVisible(true);

        gamePanel.startGameThread();

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String aboutText = "Developer: Mustafa Samet Uzay\n"
                        + "School Number: 20240702056\n"
                        + "Email: mustafasamet.uzay@std.yeditepe.edu.tr";
                JOptionPane.showMessageDialog(GameFrame.this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerNewGame();
            }
        });

        loadCustomMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerLoadCustomMap();
            }
        });

        backToMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        mapEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerMapEditor();
            }
        });

        highScores.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerHighScores();
            }
        });

        options.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] diffOptions = { "Easy", "Medium", "Hard" };
                String selection = (String) JOptionPane.showInputDialog(
                        GameFrame.this,
                        "Select Game Difficulty:",
                        "Options - Difficulty",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        diffOptions,
                        "Medium"
                );

                if (selection != null) {
                    Difficulty diff = Difficulty.MEDIUM;
                    if (selection.equals("Easy")) {
                        diff = Difficulty.EASY;
                    } else if (selection.equals("Hard")) {
                        diff = Difficulty.HARD;
                    }
                    showGameScreen();
                    if (gamePanel.isCustomMap()) {
                        gamePanel.startCustomGame(diff, gamePanel.getCustomMapPath());
                    } else {
                        gamePanel.startNewGame(diff);
                    }
                }
            }
        });

        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String helpText = "=== BATTLE CITY GAMEPLAY HELP ===\n\n"
                        + "CONTROLS:\n"
                        + "- W, A, S, D / Arrow Keys: Move player tank\n"
                        + "- SPACEBAR: Fire bullet\n"
                        + "- P: Pause / Resume the game\n\n"
                        + "OBJECTIVES & RULES:\n"
                        + "- Protect your Base (Eagle / Bird icon at the bottom).\n"
                        + "- Destroy all 20 enemy tanks to clear each stage (3 levels total).\n"
                        + "- Avoid losing all available lives; the game ends if the player or base is destroyed.\n"
                        + "- Normal bricks can be shot down by any tank. Steel walls can only be destroyed if you have collected 3 Stars.\n"
                        + "- Tanks can move under green bushes to hide themselves.\n\n"
                        + "POWER-UPS:\n"
                        + "- Tank (1UP): Gives an extra life.\n"
                        + "- Star: Upgrades your tank (1 Star = faster shots, 2 Stars = two active shots, 3 Stars = can destroy steel).\n"
                        + "- Bomb: Destroys all currently visible enemy tanks.\n"
                        + "- Clock: Freezes all enemies for a period of time.\n"
                        + "- Shovel: Places steel walls around the base temporarily.\n"
                        + "- Shield: Makes player invulnerable to damage for a period of time.";
                JOptionPane.showMessageDialog(GameFrame.this, helpText, "Help - How to Play", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void showMainMenu() {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        getContentPane().removeAll();
        setJMenuBar(null);

        mainMenuPanel = new MainMenuPanel(new MainMenuPanel.MenuSelectionListener() {
            @Override
            public void onSelect(int index) {
                if (index == 0) {
                    triggerNewGame();
                } else if (index == 1) {
                    triggerLoadCustomMap();
                } else if (index == 2) {
                    triggerMapEditor();
                } else if (index == 3) {
                    triggerHighScores();
                } else if (index == 4) {
                    System.exit(0);
                }
            }
        });

        add(mainMenuPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
        mainMenuPanel.requestFocusInWindow();
    }

    public void showGameScreen() {
        getContentPane().removeAll();
        setJMenuBar(menuBar);
        add(gameContainer, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        revalidate();
        repaint();
        gamePanel.requestFocusInWindow();
    }

    private void triggerNewGame() {
        String[] options = { "Easy", "Medium", "Hard" };
        String selection = (String) JOptionPane.showInputDialog(
                GameFrame.this,
                "Select Game Difficulty:",
                "New Game",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                "Medium"
        );

        if (selection != null) {
            Difficulty diff = Difficulty.MEDIUM;
            if (selection.equals("Easy")) {
                diff = Difficulty.EASY;
            } else if (selection.equals("Hard")) {
                diff = Difficulty.HARD;
            }
            showGameScreen();
            gamePanel.startNewGame(diff);
        } else if (mainMenuPanel != null) {
            mainMenuPanel.requestFocusInWindow();
        }
    }

    private void triggerLoadCustomMap() {
        String fileName = JOptionPane.showInputDialog(
                GameFrame.this,
                "Enter custom map file name to load (e.g., custom_map.txt):",
                "Load Custom Map",
                JOptionPane.QUESTION_MESSAGE
        );

        if (fileName != null && !fileName.isEmpty()) {
            File file = new File(fileName);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(
                        GameFrame.this,
                        "File does not exist!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                if (mainMenuPanel != null) {
                    mainMenuPanel.requestFocusInWindow();
                }
                return;
            }

            String[] options = { "Easy", "Medium", "Hard" };
            String selection = (String) JOptionPane.showInputDialog(
                    GameFrame.this,
                    "Select Game Difficulty for Custom Map:",
                    "New Game (Custom)",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    "Medium"
            );

            if (selection != null) {
                Difficulty diff = Difficulty.MEDIUM;
                if (selection.equals("Easy")) {
                    diff = Difficulty.EASY;
                } else if (selection.equals("Hard")) {
                    diff = Difficulty.HARD;
                }
                showGameScreen();
                gamePanel.startCustomGame(diff, fileName);
            } else if (mainMenuPanel != null) {
                mainMenuPanel.requestFocusInWindow();
            }
        } else if (mainMenuPanel != null) {
            mainMenuPanel.requestFocusInWindow();
        }
    }

    private void triggerMapEditor() {
        MapEditor editor = new MapEditor(GameFrame.this);
        editor.setVisible(true);
        if (mainMenuPanel != null) {
            mainMenuPanel.requestFocusInWindow();
        }
    }

    private void triggerHighScores() {
        StringBuilder scoreText = new StringBuilder("=== HIGH SCORES ===\n\n");
        try {
            File file = new File("scores.csv");
            if (!file.exists()) {
                JOptionPane.showMessageDialog(GameFrame.this, "No recorded scores.", "High Scores",
                        JOptionPane.INFORMATION_MESSAGE);
                if (mainMenuPanel != null) {
                    mainMenuPanel.requestFocusInWindow();
                }
                return;
            }

            java.util.List<String> scoreLines = new java.util.ArrayList<String>();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.isEmpty() && line.contains(",")) {
                    scoreLines.add(line);
                }
            }
            scanner.close();

            java.util.Collections.sort(scoreLines, new ScoreComparator());

            int limit = Math.min(10, scoreLines.size());
            for (int i = 0; i < limit; i++) {
                String[] parts = scoreLines.get(i).split(",");
                if (parts.length == 2) {
                    scoreText.append((i + 1)).append(". Player: ").append(parts[0])
                            .append(" | Score: ").append(parts[1]).append("\n");
                }
            }

            JOptionPane.showMessageDialog(GameFrame.this, scoreText.toString(), "High Scores",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(GameFrame.this, "There was an error reading the scores!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (mainMenuPanel != null) {
            mainMenuPanel.requestFocusInWindow();
        }
    }

    @Override
    public void onGameOver() {
        showMainMenu();
    }

    @Override
    public void onLivesChanged(int currentLives) {
        if (infoPanel != null) {
            infoPanel.repaint();
        }
    }

    @Override
    public void onLevelChanged(int currentLevel) {
        if (infoPanel != null) {
            infoPanel.repaint();
        }
    }

    @Override
    public void onScoreChanged(int score) {
        if (infoPanel != null) {
            infoPanel.repaint();
        }
    }

    private class HUDPanel extends JPanel {
        public HUDPanel() {
            setPreferredSize(new Dimension(64, 480));
            setLayout(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (ImageLoader.hudBgPattern != null) {
                g.drawImage(ImageLoader.hudBgPattern, 0, 0, getWidth(), getHeight(), null);
            }

            int startX = 16;

            int remainingEnemies = 20 - (gamePanel != null ? gamePanel.getEnemiesKilled() : 0);
            int enemyStartY = 20;
            int iconSize = 16;
            int gap = 4;
            for (int i = 0; i < remainingEnemies; i++) {
                int row = i / 2;
                int col = i % 2;
                int x = startX + col * (iconSize + gap);
                int y = enemyStartY + row * (iconSize + gap);
                if (ImageLoader.enemyIcon != null) {
                    g.drawImage(ImageLoader.enemyIcon, x, y, iconSize, iconSize, null);
                }
            }

            int ipY = 240;
            if (ImageLoader.ipSign != null) {
                g.drawImage(ImageLoader.ipSign, startX, ipY, 32, 16, null);
            }
            int playerIconY = ipY + 20;
            if (ImageLoader.playerIcon != null) {
                g.drawImage(ImageLoader.playerIcon, startX, playerIconY, 16, 16, null);
            }
            int lives = (gamePanel != null && gamePanel.getPlayer() != null) ? gamePanel.getPlayer().lives : 3;
            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospaced", Font.BOLD, 16));
            g.drawString(String.valueOf(lives), startX + 20, playerIconY + 13);

            int flagY = playerIconY + 45;
            if (ImageLoader.flagIcon != null) {
                g.drawImage(ImageLoader.flagIcon, startX, flagY, 32, 32, null);
            }

            if (ImageLoader.hudBgPattern != null) {
                g.drawImage(ImageLoader.hudBgPattern, startX + 16, flagY + 16, 16, 16, null);
            }

            int level = (gamePanel != null) ? gamePanel.getCurrentLevel() : 1;
            g.drawString(String.valueOf(level), startX + 18, flagY + 30);

            int scoreY = flagY + 65;
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            g.drawString("PTS", startX, scoreY);
            int score = (gamePanel != null) ? gamePanel.getScore() : 0;
            g.drawString(String.valueOf(score), startX, scoreY + 15);
        }
    }

}
