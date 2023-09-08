package ae.recycler.be.model;

import ae.recycler.be.enums.VehicleType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Property;
@Getter
@Setter
public class Vehicle extends BaseModel{
    @Property
    private VehicleType vehicleType;
    @Property
    private String plate;
    @Property
    private Integer capacity;
}
