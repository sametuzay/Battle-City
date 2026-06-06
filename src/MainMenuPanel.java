import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Orijinal NES tarzında tasarlanmış Ana Menü Paneli.
 * Kullanıcıların yön tuşları veya WASD ile seçim yapmasını sağlar.
 */
public class MainMenuPanel extends JPanel {
    
    private final String[] options = {
        "1 PLAYER",
        "LOAD CUSTOM MAP",
        "MAP EDITOR",
        "HIGH SCORES",
        "EXIT"
    };
    
    private int selectedIndex = 0;
    
    // Seçim yapıldığında üst pencereye (GameFrame) haber vermek için callback arayüzü
    public interface MenuSelectionListener {
        void onSelect(int index);
    }
    
    public MainMenuPanel(final MenuSelectionListener listener) {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(528, 480));
        setFocusable(true);
        
        // Klavye olaylarını dinleyen anonim iç sınıf
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                
                // Yukarı hareket (W veya Yukarı Ok)
                if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                    selectedIndex--;
                    if (selectedIndex < 0) {
                        selectedIndex = options.length - 1;
                    }
                    repaint();
                }
                // Aşağı hareket (S veya Aşağı Ok)
                else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                    selectedIndex++;
                    if (selectedIndex >= options.length) {
                        selectedIndex = 0;
                    }
                    repaint();
                }
                // Seçimi onaylama (Enter veya Boşluk)
                else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                    if (listener != null) {
                        listener.onSelect(selectedIndex);
                    }
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Logoyu ekrana ortalayacak şekilde çizdiriyoruz
        int logoW = 198;
        int logoH = 96; // Sadece BATTLE CITY yazan kısım
        
        // Genişlik yeterliyse 1.5 kat ölçekliyoruz
        double scale = 1.0;
        if (getWidth() >= 400) {
            scale = 1.5;
        }
        
        int drawW = (int) (logoW * scale);
        int drawH = (int) (logoH * scale);
        
        int logoX = (getWidth() - drawW) / 2;
        int logoY = 40;
        
        if (ImageLoader.gameLogo != null) {
            g.drawImage(ImageLoader.gameLogo, logoX, logoY, drawW, drawH, null);
        }
        
        // Seçenekleri yazdırıyoruz
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        int startY = logoY + drawH + 45;
        int startX = (getWidth() - 200) / 2; // Metinleri kabaca ortalamak için
        if (startX < 150) {
            startX = 150;
        }
        
        for (int i = 0; i < options.length; i++) {
            int optionY = startY + (i * 35);
            if (i == selectedIndex) {
                g.setColor(Color.WHITE);
                
                // Seçili seçeneğin soluna dinamik tank imlecini çiziyoruz
                if (ImageLoader.playerTank != null && ImageLoader.playerTank[Direction.RIGHT.ordinal()][0] != null) {
                    g.drawImage(ImageLoader.playerTank[Direction.RIGHT.ordinal()][0], startX - 35, optionY - 17, 24, 24, null);
                } else {
                    g.drawString(">", startX - 25, optionY);
                }
            } else {
                g.setColor(Color.GRAY);
            }
            g.drawString(options[i], startX, optionY);
        }
    }
}
