package ae.recycler.be.factories;

import ae.recycler.be.model.Address;
import java.util.Random;


public class AddressFactory {
    private static final Random random = new Random();
    public static Address build(){
        return Address.builder().humanReadableAddress(PlacesFactory.getPlace())
                .geolocation(String.valueOf(random.nextFloat())).build();
    }

}


