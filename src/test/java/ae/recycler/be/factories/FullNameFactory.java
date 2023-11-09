package ae.recycler.be.factories;


import java.util.List;
import java.util.Random;

public class FullNameFactory {
    private static final List<String> firstNames = List.of("Gale", "Lae'zel", "Shadowheart", "Karlach",
            "Astarion", "Guernicus", "Jim", "Tychus", "Tassadar", "Viconia", "Gorion", "Isobel", "Halsin", "Robert",
            "Julius", "Henry", "Ketheric", "Enrico", "Ferruccio", "Mario", "Aribeth", "Raphael");
    private static final List<String> lastNames = List.of("Waterdeep", "Gith", "Baldur", "Avernus",
            "Camarilla", "Ars", "Raynor", "Findlay", "Protoss", "DeVir", "Candlekeeep", "Thorm", "Bear", "Oppenheimer",
            "Caesar", "Jones", "Myrkul", "Fermi", "Parri", "Draghi", "de Tylmarande", "Hope"
    );
    private static final Random random = new Random();

    public static String getFirstName(){
        int i = random.nextInt(FullNameFactory.firstNames.size());
        return firstNames.get(i);
    }
    public static String getLastName(){
        int i = random.nextInt(FullNameFactory.lastNames.size());
        return lastNames.get(i);
    }
}
