import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Görsel harita editörü sınıfı.
 * Kullanıcıların 13x13 boyutunda harita çizip dosyaya kaydetmesini sağlar.
 */
public class MapEditor extends JDialog {
    private int[][] grid = new int[13][13];
    private int selectedTile = 1; // Varsayılan: Tuğla duvar (1)

    private GridPanel gridPanel;
    private JButton[] paletteButtons = new JButton[6];
    private final int[] tileTypes = { 0, 1, 2, 3, 4, 8 }; // Eraser, Brick, Steel, Water, Bush, Base
    private final String[] tileNames = { "Eraser", "Brick", "Steel", "Water", "Bush", "Base" };

    public MapEditor(Frame owner) {
        super(owner, "Map Editor", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Ana çizim alanı (Grid)
        gridPanel = new GridPanel();
        add(gridPanel, BorderLayout.CENTER);

        // Yan palet paneli (Araçlar)
        JPanel palettePanel = new JPanel(new GridLayout(6, 1, 5, 5));
        palettePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < tileNames.length; i++) {
            final int index = i;
            paletteButtons[i] = new JButton(tileNames[i]);
            paletteButtons[i].setFocusPainted(false);

            // Seçili aracı güncellemek için listener
            paletteButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedTile = tileTypes[index];
                    updatePaletteSelection();
                }
            });
            palettePanel.add(paletteButtons[i]);
        }
        updatePaletteSelection(); // Varsayılan seçimi göster
        add(palettePanel, BorderLayout.WEST);

        // Alt kontrol butonları (Dosya İşlemleri)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnNew = new JButton("New Map");
        JButton btnLoad = new JButton("Load Map");
        JButton btnSave = new JButton("Save Map");
        JButton btnClose = new JButton("Close");

        btnNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearMap();
            }
        });

        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMapFromFile();
            }
        });

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMapToFile();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        controlPanel.add(btnNew);
        controlPanel.add(btnLoad);
        controlPanel.add(btnSave);
        controlPanel.add(btnClose);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Palet üzerindeki seçili butonun kenarlığını belirginleştirir.
     */
    private void updatePaletteSelection() {
        for (int i = 0; i < tileTypes.length; i++) {
            if (tileTypes[i] == selectedTile) {
                paletteButtons[i].setBorder(new LineBorder(Color.RED, 2));
            } else {
                paletteButtons[i].setBorder(UIManager.getBorder("Button.border"));
            }
        }
    }

    /**
     * Tüm haritayı temizleyerek sıfırlar.
     */
    private void clearMap() {
        for (int r = 0; r < 13; r++) {
            for (int c = 0; c < 13; c++) {
                grid[r][c] = 0;
            }
        }
        gridPanel.repaint();
    }

    /**
     * Tıklanan koordinatlara seçili engeli yerleştirir.
     */
    private void paintTileAt(int x, int y) {
        int col = x / 32;
        int row = y / 32;
        if (row >= 0 && row < 13 && col >= 0 && col < 13) {
            grid[row][col] = selectedTile;
            gridPanel.repaint();
        }
    }

    /**
     * JOptionPane girdisi yardımıyla haritayı dosyadan yükler.
     */
    private void loadMapFromFile() {
        String fileName = JOptionPane.showInputDialog(this, "Enter map file name (e.g., map1.txt):", "Load Map",
                JOptionPane.QUESTION_MESSAGE);
        if (fileName != null && !fileName.isEmpty()) {
            File fileToLoad = new File(fileName);
            if (!fileToLoad.exists()) {
                JOptionPane.showMessageDialog(this, "File does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Scanner scanner = new Scanner(fileToLoad);
                for (int r = 0; r < 13; r++) {
                    for (int c = 0; c < 13; c++) {
                        if (scanner.hasNextInt()) {
                            grid[r][c] = scanner.nextInt();
                        }
                    }
                }
                scanner.close();
                gridPanel.repaint();
                JOptionPane.showMessageDialog(this, "Map loaded successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading map file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * JOptionPane girdisi yardımıyla haritayı dosyaya kaydeder.
     */
    private void saveMapToFile() {
        String fileName = JOptionPane.showInputDialog(this, "Enter map file name to save (e.g., map1.txt):", "Save Map",
                JOptionPane.QUESTION_MESSAGE);
        if (fileName != null && !fileName.isEmpty()) {
            File fileToSave = new File(fileName);
            try {
                PrintWriter writer = new PrintWriter(fileToSave);
                for (int r = 0; r < 13; r++) {
                    StringBuilder line = new StringBuilder();
                    for (int c = 0; c < 13; c++) {
                        line.append(grid[r][c]);
                        if (c < 12) {
                            line.append(" ");
                        }
                    }
                    writer.println(line.toString());
                }
                writer.close();
                JOptionPane.showMessageDialog(this, "Map saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving map file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Haritanın çizildiği ve fare olaylarının dinlendiği iç panel.
     */
    private class GridPanel extends JPanel {
        public GridPanel() {
            setPreferredSize(new Dimension(416, 416));
            setBackground(Color.BLACK);

            // Fare tıklama ve sürükleme olaylarını dinliyoruz (Anonim İç Sınıf
            // kullanılmıştır)
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    paintTileAt(e.getX(), e.getY());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    paintTileAt(e.getX(), e.getY());
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Matris çizimi
            for (int r = 0; r < 13; r++) {
                for (int c = 0; c < 13; c++) {
                    int x = c * 32;
                    int y = r * 32;

                    int tile = grid[r][c];

                    // Özel alan renklendirmeleri (Blok yerleştirilmeyecek spawn alanları)
                    if (tile == 0) {
                        if (r == 0 && (c == 0 || c == 6 || c == 12)) {
                            // Düşman Spawn Alanı (Koyu Kırmızı)
                            g.setColor(Color.DARK_GRAY);
                            g.fillRect(x, y, 32, 32);
                        } else if (r == 12 && c == 4) {
                            // Oyuncu Spawn Alanı (Koyu Mavi)
                            g.setColor(Color.DARK_GRAY);
                            g.fillRect(x, y, 32, 32);
                        } else if (r == 12 && c == 6) {
                            // Kartal Üs Alanı (Koyu Gri)
                            g.setColor(Color.DARK_GRAY);
                            g.fillRect(x, y, 32, 32);
                        }
                    }

                    switch (tile) {
                        case 1:
                            if (ImageLoader.brickWallFull != null) {
                                g.drawImage(ImageLoader.brickWallFull, x, y, 32, 32, null);
                            }
                            break;
                        case 2:
                            if (ImageLoader.steelWall != null) {
                                g.drawImage(ImageLoader.steelWall, x, y, 32, 32, null);
                            }
                            break;
                        case 3:
                            if (ImageLoader.water != null) {
                                g.drawImage(ImageLoader.water, x, y, 32, 32, null);
                            }
                            break;
                        case 4:
                            if (ImageLoader.bush != null) {
                                g.drawImage(ImageLoader.bush, x, y, 32, 32, null);
                            }
                            break;
                        case 8:
                            if (ImageLoader.baseEagle != null) {
                                g.drawImage(ImageLoader.baseEagle, x, y, 32, 32, null);
                            }
                            break;
                    }

                    // Kılavuz Grid Çizgileri
                    g.setColor(new Color(60, 60, 60));
                    g.drawRect(x, y, 32, 32);
                }
            }
        }
    }
}
