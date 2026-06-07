import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel {
    
    private final String[] options = {
        "1 PLAYER",
        "LOAD CUSTOM MAP",
        "MAP EDITOR",
        "HIGH SCORES",
        "EXIT"
    };
    
    private int selectedIndex = 0;
    
    public interface MenuSelectionListener {
        void onSelect(int index);
    }
    
    public MainMenuPanel(final MenuSelectionListener listener) {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(528, 480));
        setFocusable(true);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                
                if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                    selectedIndex--;
                    if (selectedIndex < 0) {
                        selectedIndex = options.length - 1;
                    }
                    repaint();
                }
                else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                    selectedIndex++;
                    if (selectedIndex >= options.length) {
                        selectedIndex = 0;
                    }
                    repaint();
                }
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
        
        int logoW = 198;
        int logoH = 96;
        
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
        
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        int startY = logoY + drawH + 45;
        int startX = (getWidth() - 200) / 2;
        if (startX < 150) {
            startX = 150;
        }
        
        for (int i = 0; i < options.length; i++) {
            int optionY = startY + (i * 35);
            if (i == selectedIndex) {
                g.setColor(Color.WHITE);
                
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
