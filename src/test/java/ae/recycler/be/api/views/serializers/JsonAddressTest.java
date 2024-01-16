package ae.recycler.be.api.views.serializers;


import ae.recycler.be.factories.AddressFactory;
import ae.recycler.be.model.Address;
import org.junit.jupiter.api.Test;


public class JsonAddressTest {

    @Test
    public void testToMap(){
        Address address = AddressFactory.buildRandom();
        JsonAddress jsonAddress = JsonAddress.fromAddress(address);
        assert address.toMap().equals(jsonAddress.toMap());

    }
}
