import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {// bu ne işe yarıyo mesela anla
            @Override
            public void run() {
                GameFrame frame = new GameFrame();// runlamak için bunu mu oluşturmak gerek, hangisi runlayan fonksiyonu
                                                  // çağırıyor anla.
            }
        });

    }
}
