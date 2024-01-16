package ae.recycler.be.factories;

import ae.recycler.be.model.Address;
import lombok.Builder;

import java.util.Random;


@Builder
public class AddressFactory {
    private static final Random random = new Random();
    private Double lat, lng;
    private String emirate, area, streetName, houseOrAptNumber, floor, nickname, buildingOrPlaceName;
    public static Address buildRandom(){
        return new Address(null, 1.0, 2.0, BuildingOrPlaceFactory.getBuilding(), "Dubai",
                "Braih Street", PlacesFactory.getPlace(), "1010", "10", "home");
    }
    public static Address build(Float lat, Float lng) {
        return Address.builder().buildingOrPlaceName(PlacesFactory.getPlace())
                .lat(lat == null ? random.nextDouble() : lat)
                .lng(lng == null ? random.nextDouble() : lng).build();
    }


}


