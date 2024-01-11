package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


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
    private double lat;
    @Property
    private double lng;
    @Property
    private String humanReadableAddress;
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


    public Address(UUID id, double lat, double lng, String humanReadableAddress){
        this.id = id;
        this.humanReadableAddress = humanReadableAddress;
        this.lat = lat;
        this.lng = lng;
    }
    public static Address fromAddress(ae.recycler.be.api.views.serializers.Address address){
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
}
