package ae.recycler.be.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@Builder
public class Driver extends BaseModel{
    @GeneratedValue
    @Id
    private UUID id;
    @Property
    private String name;
    @Property
    private boolean available;
}
