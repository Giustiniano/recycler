package ae.recycler.be.factories;

import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

public class EmailFactory {
    private static final List<String> firstNames = List.of("Gale", "Lae'zel", "Shadowheart", "Karlach",
            "Astarion", "Guernicus", "Jim", "Tychus", "Tassadar", "Viconia", "Gorion", "Isobel", "Halsin", "Robert",
            "Julius", "Henry", "Ketheric", "Enrico", "Ferruccio", "Mario", "Aribeth", "Raphael");
    private static final List<String> lastNames = List.of("Waterdeep", "Gith", "Baldur", "Avernus",
            "Camarilla", "Ars", "Raynor", "Findlay", "Protoss", "DeVir", "Candlekeeep", "Thorm", "Bear", "Oppenheimer",
            "Caesar", "Jones", "Myrkul", "Fermi", "Parri", "Draghi", "de Tylmarande", "Hope"
    );

    private static final List<String> domains = List.of("gmail", "outlook", "altavista", "hotmail", "hotbot", "lycos",
            "geocities", "fastmail", "neverwinter", "icq");

    private static final List<String> ttlds = List.of("com", "org", "net", "edu", "it", "ua", "fr", "my");

    public static String personalEmail(){
        Random random = new Random();
        return new StringBuilder().append(firstNames.get(random.nextInt(firstNames.size()))).append("_")
                .append(lastNames.get(random.nextInt(lastNames.size()))).append("@")
                .append(random.nextInt(domains.size())).append(".").append(random.nextInt(ttlds.size())).toString();
    }
}
