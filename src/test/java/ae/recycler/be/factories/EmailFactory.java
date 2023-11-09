package ae.recycler.be.factories;


import java.util.List;
import java.util.Random;


public class EmailFactory {


    private static final List<String> domains = List.of("gmail", "outlook", "altavista", "hotmail", "hotbot", "lycos",
            "geocities", "fastmail", "neverwinter", "icq");

    private static final List<String> ttlds = List.of("com", "org", "net", "edu", "it", "ua", "fr", "my");

    public static String personalEmail(){
        Random random = new Random();
        return new StringBuilder().append(FullNameFactory.getFirstName()).append("_")
                .append(FullNameFactory.getLastName()).append("@")
                .append(random.nextInt(domains.size())).append(".").append(random.nextInt(ttlds.size())).toString();
    }
}
