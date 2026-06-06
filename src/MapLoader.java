import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class MapLoader {
    public static void loadMap(String filePath, List<GameObject> obstacles, PlayerBase playerBase) {
        try (Scanner scanner = new Scanner(new File(filePath))) {

            for (int row = 0; row < 13; row++) {
                for (int column = 0; column < 13; column++) {

                    if (!scanner.hasNext())
                        break;

                    int obstacleType = scanner.nextInt();

                    int x = column * 32;
                    int y = row * 32;

                    switch (obstacleType) {
                        case 1:
                            obstacles.add(new BrickWall(x, y, 32, 32));
                            break;
                        case 2:
                            obstacles.add(new SteelWall(x, y, 32, 32));
                            break;
                        case 3:
                            obstacles.add(new Water(x, y, 32, 32));
                            break;
                        case 4:
                            obstacles.add(new Bush(x, y, 32, 32));
                            break;
                        case 8:
                            playerBase.x = x;
                            playerBase.y = y;
                            break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
