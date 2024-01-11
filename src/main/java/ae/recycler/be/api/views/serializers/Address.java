package ae.recycler.be.api.views.serializers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class Address {
    private UUID id;
    private Double lat, lng;
    private String humanReadableAddress, emirate, streetName, area, houseOrAptNumber, floor;
    public static Address fromAddress(ae.recycler.be.model.Address address){
        return new Address(address.getId(), address.getLat(), address.getLng(), address.getHumanReadableAddress(),
                address.getEmirate(), address.getStreetName(), address.getArea(), address.getHouseOrAptNumber(),
                address.getFloor());
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("lat", lat);
        map.put("lng", lng);
        map.put("humanReadableAddress", humanReadableAddress);
        map.put("emirate", emirate);
        map.put("streetName", streetName);
        map.put("area", area);
        map.put("houseOrAptNumber", houseOrAptNumber);
        map.put("floor", floor);
        return map;
    }
}
