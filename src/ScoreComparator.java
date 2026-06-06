import java.util.Comparator;

/**
 * Skorları büyükten küçüğe sıralamak için kullanılan Comparator sınıfı.
 * CSV dosyasından okunan satırları (Format: isim,skor) karşılaştırır.
 */
public class ScoreComparator implements Comparator<String> {
    @Override
    public int compare(String line1, String line2) {
        try {
            // Satırları virgüle göre ayırıp skor kısımlarını alıyoruz
            String[] parts1 = line1.split(",");
            String[] parts2 = line2.split(",");
            
            // Skor değerlerini tamsayıya dönüştürüyoruz
            int score1 = Integer.parseInt(parts1[1]);
            int score2 = Integer.parseInt(parts2[1]);
            
            // Büyükten küçüğe (azalan sırada) sıralama için score2 ile score1 karşılaştırılır
            return Integer.compare(score2, score1);
        } catch (Exception e) {
            // Herhangi bir okuma/dönüştürme hatasında sıralamayı bozmamak için 0 döner
            return 0;
        }
    }
}
