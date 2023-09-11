package ae.recycler.be.model;

import ae.recycler.be.enums.VehicleType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

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
}
