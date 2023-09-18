package ae.recycler.be.factories;

import java.util.List;
import java.util.Random;

public class PlacesFactory {
    private static final Random random = new Random();
    private static final List<String> places = List.of("Gotham City", "Neverwinter", "Baldur's Gate",
            "Waterdeep", "Wayne Tower", "Jumeirah Beach Residence", "Dubai Marina", "Dubai Harbor", "Dubai Creek",
            "Rivington", "Moonrise Towers", "Sorcerous Sundries", "Wyrm's Crossing", "Gythianky Creche",
            "Burj Khalifa", "Deira", "La mer");

    public static String getPlace(){
        return places.get(random.nextInt(places.size()));
    }
}
