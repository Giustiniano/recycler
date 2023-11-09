package ae.recycler.be.api.views.serializers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Address {
    private double lat, lng;
    private String humanReadableAddress;
    public static Address fromAddress(ae.recycler.be.model.Address address){
        return new Address(address.getLat(), address.getLng(), address.getHumanReadableAddress());
    }
}
