package ae.recycler.be.model;

import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.enums.VehicleType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Node
public class Vehicle extends BaseModel{
    @GeneratedValue
    @Id
    private UUID id;
    @Property
    private VehicleType vehicleType;
    @Property
    private String plate;
    @Property
    private Integer capacity;
    @Property
    private VehicleStatus status;
    @Property
    private List<Order> assignedOrders;
    @Property
    private float lat;
    @Property
    private float lng;
    @Relationship(type = "HAS_DRIVER", direction = Relationship.Direction.OUTGOING)
    private Driver driver;
    @Property
    private float depotLat;
    @Property
    private float depotLng;
}
