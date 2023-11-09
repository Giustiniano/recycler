package ae.recycler.be.factories;

import ae.recycler.be.model.Address;

import java.util.List;

public class GeocodedPlaces {

    public static final Address BURJ_KHALIFA = new Address(
            null, 25.197197, 46.309532, "Burj Khalifa");
    public static final Address BURJ_AL_ARAB = new Address(
            null, 25.1415548,55.1836908, "Burj Al Arab");
    public static final Address DXB_TERMINAL_3 = new Address(
            null, 25.0672222,55.095198,"DXB Terminal 3" );
    public static final Address EMIRATES_BUSINESS_CLASS_LOUNGE = new Address(
            null, 25.2515272,55.3377994, "Emirates Business Class Lounge");
    public static final Address MERCATO_SHOPPING_MALL = new Address(
            null,25.2167981,55.2276385, "Mercato Shopping Mall");
    public static final Address MALL_OF_THE_EMIRATES = new Address(
            null,25.118107,55.1980331, "Mall of the Emirates");
    public static final Address RAFFLES_THE_PALM = new Address(
            null, 25.1103955,55.1072183, "Raffles the Palm");
    public static final Address BUSINESS_BAY = new Address(
            null, 25.1850084,55.2537523, "Business Bay");

    public static final Address MARINA_MALL = new Address(
            null, 25.0763518,55.1393844, "Marina Mall");
    public static final Address AMAZON_WAREHOUSE_DUBAI_SOUTH = new Address(
            null, 24.8994493,55.0066217, "Amazon warehouse Dubai South");

    public static final List<Address> PICKUP_LOCATIONS = List.of(BURJ_KHALIFA, BURJ_AL_ARAB, DXB_TERMINAL_3,
            EMIRATES_BUSINESS_CLASS_LOUNGE, MERCATO_SHOPPING_MALL, MALL_OF_THE_EMIRATES, RAFFLES_THE_PALM, BUSINESS_BAY,
            MARINA_MALL);

}
