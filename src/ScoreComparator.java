import java.util.Comparator;

public class ScoreComparator implements Comparator<String> {
    @Override
    public int compare(String line1, String line2) {
        try {
            String[] parts1 = line1.split(",");
            String[] parts2 = line2.split(",");
            
            int score1 = Integer.parseInt(parts1[1]);
            int score2 = Integer.parseInt(parts2[1]);
            
            return Integer.compare(score2, score1);
        } catch (Exception e) {
            return 0;
        }
    }
}
