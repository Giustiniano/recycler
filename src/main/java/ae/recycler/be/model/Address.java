package ae.recycler.be.model;

import ae.recycler.be.api.views.serializers.JsonAddress;
import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


@Node
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Address{
    @Id
    @GeneratedValue
    private UUID id;
    @Property
    private Double lat;
    @Property
    private Double lng;
    @Property
    private String buildingOrPlaceName;
    @Property
    private String emirate;
    @Property
    private String streetName;
    @Property
    private String area;
    @Property
    private String houseOrAptNumber;
    @Property
    private String floor;
    @Property
    private String nickname;



    public Address(UUID id, double lat, double lng, String buildingOrPlaceName){
        this.id = id;
        this.buildingOrPlaceName = buildingOrPlaceName;
        this.lat = lat;
        this.lng = lng;
    }
    public static Address fromAddress(JsonAddress address){
        Address modelAddress = new Address();
        Arrays.stream(address.getClass().getMethods()).filter(method -> method.getName().startsWith("get")).
                forEach(getter -> {
                    try {
                        Object value = getter.invoke(address);
                        if(value != null){
                            modelAddress.getClass().getMethod(getter.getName().replace("get", "set"),
                                    getter.getReturnType()).invoke(modelAddress, value);
                        }
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
        return modelAddress;
    }
    public Map<String, Object> toMap(){
        Map<String, Object> addressMap = new HashMap<>();
        Arrays.stream(this.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Property.class) != null || field.getAnnotation(Id.class) != null)
                .filter(propertyField -> propertyField.canAccess(this)).forEach(field -> {
                    try {
                        addressMap.put(field.getName(), field.get(this));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        return addressMap;
    }
}
