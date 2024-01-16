package ae.recycler.be.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuildingOrPlaceFactory {

    private static List<String> buildings;
    private static Random random = new Random();

    static {
        buildings= new ArrayList<>();
        for(String p: List.of("Sadaf", "Murjan", "Bahar", "Amwaj", "Urbana", "Golf Links", "Expo Villas")){
            for(int i = 0; i<8; i++){
                buildings.add("%s %d".formatted(p, i));
            }
        }
    }
    public static String getBuilding() {
        return buildings.get(random.nextInt(buildings.size()));
    }
}
