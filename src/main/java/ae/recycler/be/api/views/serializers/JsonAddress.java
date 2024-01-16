package ae.recycler.be.api.views.serializers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public class JsonAddress {
    private UUID id;
    private Double lat, lng;
    private String buildingOrPlaceName, emirate, streetName, area, houseOrAptNumber, floor, nickname;
    public static JsonAddress fromAddress(ae.recycler.be.model.Address address){
        return new JsonAddress(address.getId(), address.getLat(), address.getLng(), address.getBuildingOrPlaceName(),
                address.getEmirate(), address.getStreetName(), address.getArea(), address.getHouseOrAptNumber(),
                address.getFloor(), address.getNickname());
    }

    public static List<JsonAddress> fromAddress(List<ae.recycler.be.model.Address> addresses){
        return addresses.stream().map(JsonAddress::fromAddress).collect(Collectors.toList());
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        Arrays.stream(this.getClass().getDeclaredFields()).filter(field -> field.canAccess(this))
                .forEach(accessibleField -> {
                    try {
                        map.put(accessibleField.getName(), accessibleField.get(this));
                    } catch (IllegalAccessException e) {
                        // this should not happen because we filter out non-accessible fields beforehand.
                        throw new RuntimeException(e);
                    }
                });
        return map;
    }
}
