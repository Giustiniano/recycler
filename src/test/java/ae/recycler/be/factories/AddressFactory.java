package ae.recycler.be.factories;

import ae.recycler.be.model.Address;
import java.util.Random;


public class AddressFactory {
    private static final Random random = new Random();
    public static Address build(){
        return AddressFactory.build(null, null);
    }
    public static Address build(Float lat, Float lng){
        return Address.builder().humanReadableAddress(PlacesFactory.getPlace())
                .lat(lat == null ? random.nextFloat() : lat)
                .lng(lng == null ? random.nextFloat() : lng).build();
    }

}


